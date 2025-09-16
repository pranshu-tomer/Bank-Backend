package com.voltrex.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

public record AccountResponse(
        String number,              // accountNumber or cardNumber
        String type,                // "SAVINGS", "CURRENT", "SALARY", or "CREDIT_CARD"
        BigDecimal balance,         // for accounts, null for credit card
        BigDecimal interestRate,    // for accounts, null for credit card
        LocalDateTime openedAt,     // for accounts, null for credit card
        BigDecimal minimumBalance,  // for accounts, null for credit card
        BigDecimal monthlyFee,      // for accounts, null for credit card
        BigDecimal monthIn,         // for accounts, null for credit card
        BigDecimal monthOut,        // for accounts, null for credit card
        Boolean primaryAccount,     // for accounts, null for credit card
        Boolean transactionAlert
) {}
