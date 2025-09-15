package com.voltrex.bank.services;

import com.voltrex.bank.dto.TransferByAccountRequest;
import com.voltrex.bank.dto.TransferByReceiverRequest;
import com.voltrex.bank.entities.Account;
import com.voltrex.bank.entities.Transaction;
import com.voltrex.bank.entities.User;
import com.voltrex.bank.exception.InsufficientFundsException;
import com.voltrex.bank.exception.LimitExceededException;
import com.voltrex.bank.exception.NotFoundException;
import com.voltrex.bank.exception.NotOwnerException;
import com.voltrex.bank.exception.TransferException;
import com.voltrex.bank.repositories.AccountRepository;
import com.voltrex.bank.repositories.TransactionRepository;
import com.voltrex.bank.repositories.UserRepository;
import com.voltrex.bank.utils.ReferenceGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepo;
    private final TransactionRepository txnRepo;
    private final UserRepository userRepo;
    private final ReferenceGenerator refGen; // your bean (UUID-based)

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    /**
     * Transfer using receiver's account number + name verification.
     */
    @Transactional
    public String transferByAccount(TransferByAccountRequest req, User currentUser) {
        // basic validation (DTO does @Valid, but double-check)
        BigDecimal amount = req.getAmount();

        // lock accounts in deterministic order to reduce deadlocks
        String accA = req.getFromAccountNumber();
        String accB = req.getToAccountNumber();
        if (accA == null || accB == null) throw new TransferException("Account numbers required");

        boolean swap = accA.compareTo(accB) > 0;

        Account fromAccount;
        Account toAccount;

        if (!swap) {
            fromAccount = accountRepo.findByAccountNumberForUpdate(accA).orElseThrow(() -> new NotFoundException("Sender account not found"));
            toAccount = accountRepo.findByAccountNumberForUpdate(accB).orElseThrow(() -> new NotFoundException("Receiver account not found"));
        } else {
            toAccount = accountRepo.findByAccountNumberForUpdate(accB).orElseThrow(() -> new NotFoundException("Receiver account not found"));
            fromAccount = accountRepo.findByAccountNumberForUpdate(accA).orElseThrow(() -> new NotFoundException("Sender account not found"));
        }

        // Ownership check
        if (!fromAccount.getOwner().getId().equals(currentUser.getId())) {
            throw new NotOwnerException("You are not allowed to transfer from this account");
        }

        // Verify receiver name
        if (!matchesName(toAccount.getOwner(), req.getToAccountName())) {
            throw new TransferException("Receiver details are incorrect");
        }

        // Insufficient funds
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient balance");
        }

        // Check daily withdrawal/deposit limits
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal alreadyWithdrawnToday = txnRepo.sumWithdrawnBetween(fromAccount.getId(), dayStart, dayEnd);
        BigDecimal alreadyDepositedToday = txnRepo.sumDepositedBetween(toAccount.getId(), dayStart, dayEnd);

        BigDecimal senderDailyLimit = resolveMaxDailyWithdrawal(fromAccount);
        if (senderDailyLimit != null && alreadyWithdrawnToday.add(amount).compareTo(senderDailyLimit) > 0) {
            throw new LimitExceededException("Daily withdrawal limit exceeded for sender");
        }

        BigDecimal receiverDailyLimit = resolveMaxDailyDeposit(toAccount);
        if (receiverDailyLimit != null && alreadyDepositedToday.add(amount).compareTo(receiverDailyLimit) > 0) {
            throw new LimitExceededException("Daily deposit limit exceeded for receiver");
        }

        // Perform debit/credit
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        // persist accounts & transaction
        accountRepo.save(fromAccount);
        accountRepo.save(toAccount);

        Transaction tx = Transaction.builder()
                .referenceNumber(refGen.generate())
                .amount(amount)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .description(req.getDescription())
                .type(req.getType())
                .status("COMPLETED")
                .build();

        txnRepo.save(tx);

        return tx.getReferenceNumber();
    }

    /**
     * Transfer using receiver identifier (CRN or email) — deposits to receiver's primary account.
     */
    @Transactional
    public String transferByReceiver(TransferByReceiverRequest req, User currentUser) {
        BigDecimal amount = req.getAmount();

        // find receiver user by CRN or email
        Optional<User> maybe = userRepo.findByCrn(req.getReceiverIdentifier());
        if (maybe.isEmpty()) maybe = userRepo.findByEmail(req.getReceiverIdentifier());
        if (maybe.isEmpty()) throw new NotFoundException("Receiver not found");

        User receiver = maybe.get();

        if (!matchesName(receiver, req.getReceiverName())) {
            throw new TransferException("Receiver details are incorrect");
        }

        // lock receiver primary account
        Account receiverAccount = accountRepo.findPrimaryByOwnerIdForUpdate(receiver.getId())
                .orElseThrow(() -> new NotFoundException("Receiver does not have a primary account"));

        // lock sender (deterministic order)
        String accA = req.getFromAccountNumber();
        String accB = receiverAccount.getAccountNumber();
        boolean swap = accA.compareTo(accB) > 0;

        Account fromAccount;
        Account toAccount = receiverAccount;

        if (!swap) {
            fromAccount = accountRepo.findByAccountNumberForUpdate(accA).orElseThrow(() -> new NotFoundException("Sender account not found"));
        } else {
            fromAccount = accountRepo.findByAccountNumberForUpdate(accA).orElseThrow(() -> new NotFoundException("Sender account not found"));
        }

        // ownership check
        if (!fromAccount.getOwner().getId().equals(currentUser.getId())) {
            throw new NotOwnerException("You are not allowed to transfer from this account");
        }

        // Insufficient funds
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient balance");
        }

        // daily limits
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        LocalDateTime dayEnd = LocalDate.now().atTime(LocalTime.MAX);

        BigDecimal alreadyWithdrawnToday = txnRepo.sumWithdrawnBetween(fromAccount.getId(), dayStart, dayEnd);
        BigDecimal alreadyDepositedToday = txnRepo.sumDepositedBetween(toAccount.getId(), dayStart, dayEnd);

        BigDecimal senderDailyLimit = resolveMaxDailyWithdrawal(fromAccount);
        if (senderDailyLimit != null && alreadyWithdrawnToday.add(amount).compareTo(senderDailyLimit) > 0) {
            throw new LimitExceededException("Daily withdrawal limit exceeded for sender");
        }

        BigDecimal receiverDailyLimit = resolveMaxDailyDeposit(toAccount);
        if (receiverDailyLimit != null && alreadyDepositedToday.add(amount).compareTo(receiverDailyLimit) > 0) {
            throw new LimitExceededException("Daily deposit limit exceeded for receiver");
        }

        // Perform debit/credit
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        accountRepo.save(fromAccount);
        accountRepo.save(toAccount);

        // after you have debited/credited the accounts and saved accountRepo.save(...)
        Transaction tx = Transaction.builder()
                .referenceNumber(refGen.generate())
                .amount(amount)
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .description(req.getDescription())
                .status("COMPLETED")
                .type(req.getType()) // ensure your DTO supplies type
                // set snapshots:
                .build();

        log.info("Before saving tx: fromBalanceAfter={}, toBalanceAfter={}", fromAccount.getBalance(), toAccount.getBalance());
        log.info("Setting tx.fromAccountBalanceAfter={}, tx.toAccountBalanceAfter={}", fromAccount.getBalance(), toAccount.getBalance());
        txnRepo.save(tx);

        tx.setFromAccountBalanceAfter(fromAccount.getBalance());
        tx.setToAccountBalanceAfter(toAccount.getBalance());
        txnRepo.save(tx);

        return tx.getReferenceNumber();
    }

    private BigDecimal resolveMaxDailyWithdrawal(Account acc) {
        if (acc == null) return null;
        try {
            if (acc.getType() != null && acc.getType().getMaxDailyWithdrawal() != null) {
                return acc.getType().getMaxDailyWithdrawal();
            }
        } catch (Exception ignored) { }
        return null;
    }

    private BigDecimal resolveMaxDailyDeposit(Account acc) {
        if (acc == null) return null;
        try {
            if (acc.getType() != null && acc.getType().getMaxDailyDeposit() != null) {
                return acc.getType().getMaxDailyDeposit();
            }
        } catch (Exception ignored) { }
        return null;
    }

    private boolean matchesName(User user, String provided) {
        if (provided == null || provided.isBlank()) return false;
        String actual = (user.getFirstName()==null ? "" : user.getFirstName().trim()) +
                " " + (user.getLastName()==null ? "" : user.getLastName().trim());
        String a = actual.trim().replaceAll("\\s+", " ").toLowerCase();
        String p = provided.trim().replaceAll("\\s+", " ").toLowerCase();
        if (a.equals(p)) return true;
        // allow swapped names
        String[] ap = a.split(" ");
        String[] pp = p.split(" ");
        if (ap.length >=2 && pp.length >= 2) {
            String aRev = ap[ap.length-1] + " " + ap[0];
            if (aRev.equals(p)) return true;
        }
        return false;
    }
}
