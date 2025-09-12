package com.voltrex.bank.services;

import com.voltrex.bank.dto.AccountResponse;
import com.voltrex.bank.entities.Account;
import com.voltrex.bank.repositories.AccountRepository;
import com.voltrex.bank.repositories.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepo;
    private final TransactionRepository txnRepo;

    public List<AccountResponse> getAccountsForUser(Long userId) {
        List<Account> accounts = accountRepo.findByOwnerId(userId);

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime from = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime to = currentMonth.atEndOfMonth().atTime(23,59,59, 999_999_999);

        return accounts.stream().map(acc -> {
            BigDecimal monthIn = txnRepo.sumDepositedBetween(acc.getId(), from, to);
            BigDecimal monthOut = txnRepo.sumWithdrawnBetween(acc.getId(), from, to);

            return new AccountResponse(
                    acc.getAccountNumber(),
                    acc.getType().name(),
                    acc.getBalance(),
                    acc.getInterestRate(),
                    acc.getOpenedAt(),
                    acc.getMinimumBalance(),
                    acc.getMonthlyFee(),
                    monthIn,
                    monthOut,
                    acc.isPrimaryAccount()
            );
        }).toList();
    }
}
