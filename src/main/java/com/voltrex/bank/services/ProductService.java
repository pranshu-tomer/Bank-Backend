package com.voltrex.bank.services;

import com.voltrex.bank.dto.ProductResponse;
import com.voltrex.bank.entities.Account;
import com.voltrex.bank.entities.User;
import com.voltrex.bank.entities.AccountType;
import com.voltrex.bank.repositories.CreditCardRepository;
import com.voltrex.bank.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final UserRepository userRepo;
    private final CreditCardRepository creditCardRepo;

    @Transactional(readOnly = true)
    public List<ProductResponse> getAvailableProductsForUser(Long userId) {
        User managedUser = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // owned accounts
        Set<AccountType> owned = Optional.ofNullable(managedUser.getAccounts()).orElse(Collections.emptyList())
                .stream()
                .map(Account::getType)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<ProductResponse> available = new ArrayList<>();

        // add missing account types
        Arrays.stream(AccountType.values())
                .filter(t -> !owned.contains(t))
                .map(this::toProductDto)
                .forEach(available::add);

        // add credit card if user doesn't already have one
        boolean hasCard = creditCardRepo.findByUserId(userId).isPresent();
        if (!hasCard) {
            available.add(creditCardProduct());
        }

        return available;
    }

    private ProductResponse toProductDto(AccountType t) {
        return new ProductResponse(
                t.name(),
                t.getOpeningCharge(),
                t.getInterestRate(),
                t.getMonthlyFee(),
                t.getMinimumBalance(),
                t.getMinimumBalancePenalty(),
                t.getMaxDailyWithdrawal(),
                t.getMaxDailyDeposit(),
                null // no creditLimit for normal accounts
        );
    }

    private ProductResponse creditCardProduct() {
        return new ProductResponse(
                "CREDIT_CARD",
                BigDecimal.ZERO, // opening charge
                BigDecimal.ZERO, // interestRate (handled separately for credit card)
                BigDecimal.ZERO, // monthlyFee
                BigDecimal.ZERO, // minBalance
                BigDecimal.ZERO, // minBalancePenalty
                BigDecimal.ZERO, // maxDailyWithdrawal (not applicable)
                BigDecimal.ZERO, // maxDailyDeposit (not applicable)
                new BigDecimal("50000.00") // default credit limit
        );
    }
}

