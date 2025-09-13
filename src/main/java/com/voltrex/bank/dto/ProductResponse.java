package com.voltrex.bank.dto;

import com.voltrex.bank.entities.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private String type; // "SAVINGS", "CURRENT", "SALARY", or "CREDIT_CARD"

    private BigDecimal openingCharge;
    private BigDecimal interestRate;
    private BigDecimal monthlyFee;
    private BigDecimal minimumBalance;
    private BigDecimal minimumBalancePenalty;
    private BigDecimal maxDailyWithdrawal;
    private BigDecimal maxDailyDeposit;

    // Only relevant for credit card
    private BigDecimal creditLimit;
}

