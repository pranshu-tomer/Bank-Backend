package com.voltrex.bank.services;

import com.voltrex.bank.dto.AccountResponse;
import com.voltrex.bank.dto.CreateAccountRequest;
import com.voltrex.bank.entities.Account;
import com.voltrex.bank.entities.AccountType;
import com.voltrex.bank.entities.User;
import com.voltrex.bank.repositories.AccountRepository;
import com.voltrex.bank.repositories.TransactionRepository;
import com.voltrex.bank.repositories.UserRepository;
import com.voltrex.bank.utils.AccountNumberGenerator;
import com.voltrex.bank.utils.CardNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepo;
    private final TransactionRepository txnRepo;
    private final AccountNumberGenerator accountNumberGenerator;
    private final UserRepository userRepo;
    private final CardNumberGenerator cardNumberGenerator;

    public List<AccountResponse> getAccountsForUser(Long userId) {
        List<Account> accounts = accountRepo.findByOwnerId(userId);

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime from = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime to = currentMonth.atEndOfMonth().atTime(23,59,59, 999_999_999);

        List<AccountResponse> responses = new ArrayList<>();

        // Normal bank accounts
        for (Account acc : accounts) {
            BigDecimal monthIn = txnRepo.sumDepositedBetween(acc.getId(), from, to);
            BigDecimal monthOut = txnRepo.sumWithdrawnBetween(acc.getId(), from, to);

            responses.add(new AccountResponse(
                    acc.getAccountNumber(),
                    acc.getType().name(),
                    acc.getBalance(),
                    acc.getInterestRate(),
                    acc.getOpenedAt(),
                    acc.getMinimumBalance(),
                    acc.getMonthlyFee(),
                    monthIn,
                    monthOut,
                    acc.isPrimaryAccount(),
                    acc.isTransactionAlert()
            ));
        }

        return responses;
    }


    @Transactional
    public String createAccount(User currentUser, CreateAccountRequest req) {
        User managedUser = userRepo.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        String typeRaw = req.getType().toUpperCase();

        // ---- Normal Account creation flow ----
        AccountType type = AccountType.valueOf(typeRaw);

        boolean exists = managedUser.getAccounts().stream()
                .anyMatch(a -> a.getType() == type);
        if (exists) {
            throw new IllegalStateException("You already have a " + type.name() + " account");
        }

        if (managedUser.getAccounts().size() >= 3) {
            throw new IllegalStateException("You already have all account types");
        }

        String accNumber;
        int attempts = 0;
        do {
            accNumber = accountNumberGenerator.generate12Digit();
            attempts++;
            if (attempts > 10_000) throw new IllegalStateException("Cannot generate unique account number");
        } while (accountRepo.existsByAccountNumber(accNumber));

        Account newAcc = Account.builder()
                .accountNumber(accNumber)
                .owner(managedUser)
                .type(type)
                .balance(BigDecimal.ZERO)
                .primaryAccount(false)
                .openedAt(java.time.LocalDateTime.now())
                .build();

        if (type.getOpeningCharge() != null && type.getOpeningCharge().compareTo(BigDecimal.ZERO) > 0) {
            newAcc.setBalance(newAcc.getBalance().subtract(type.getOpeningCharge()));
        }

        accountRepo.save(newAcc);
        managedUser.getAccounts().add(newAcc);
        userRepo.save(managedUser);

        return accNumber;
    }

}

