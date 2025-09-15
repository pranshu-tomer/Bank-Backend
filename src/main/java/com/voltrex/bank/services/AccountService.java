package com.voltrex.bank.services;

import com.voltrex.bank.dto.AccountResponse;
import com.voltrex.bank.dto.CreateAccountRequest;
import com.voltrex.bank.entities.Account;
import com.voltrex.bank.entities.AccountType;
import com.voltrex.bank.entities.CreditCard;
import com.voltrex.bank.entities.User;
import com.voltrex.bank.repositories.AccountRepository;
import com.voltrex.bank.repositories.CreditCardRepository;
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
    private final CreditCardRepository creditCardRepository;
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
                    acc.isTransactionAlert(),
                    null, // creditLimit
                    null, // creditUsed
                    null, // expiryDate
                    null  // issuedAt
            ));
        }

        // Credit card (optional)
        creditCardRepository.findByUserId(userId).ifPresent(card -> {
            responses.add(new AccountResponse(
                    card.getCardNumber(),
                    "CREDIT_CARD",
                    null,                // balance not applicable
                    null,                // interestRate not applicable
                    null,                // openedAt not applicable
                    null,                // minimumBalance not applicable
                    null,                // monthlyFee not applicable
                    null,                // monthIn not applicable
                    null,                // monthOut not applicable
                    null,                // primaryAccount not applicable
                    false,
                    card.getCreditLimit(),
                    card.getCreditUsed(),
                    card.getExpiryDate(),
                    card.getIssuedAt()
            ));
        });

        return responses;
    }


    @Transactional
    public String createAccount(User currentUser, CreateAccountRequest req) {
        User managedUser = userRepo.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        String typeRaw = req.getType().toUpperCase();

        if ("CREDIT_CARD".equals(typeRaw)) {
            // check if user already has one
            if (creditCardRepository.findByUserId(managedUser.getId()).isPresent()) {
                throw new IllegalStateException("You already have a credit card");
            }

            // generate card number (example: 16 digits)
            String cardNumber;
            int attempts = 0;
            do {
                cardNumber = cardNumberGenerator.generate16Digit();
                attempts++;
                if (attempts > 10_000) throw new IllegalStateException("Cannot generate unique card number");
            } while (creditCardRepository.existsByCardNumber(cardNumber));

            CreditCard card = CreditCard.builder()
                    .cardNumber(cardNumber)
                    .user(managedUser)
                    .expiryDate(java.time.LocalDate.now().plusYears(5))
                    .active(true)
                    .creditLimit(new BigDecimal("50000.00")) // default
                    .creditUsed(BigDecimal.ZERO)
                    .build();

            creditCardRepository.save(card);
            return cardNumber;
        }

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

