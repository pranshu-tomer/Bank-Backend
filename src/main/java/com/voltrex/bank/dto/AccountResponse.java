package com.voltrex.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        String accountNumber,
        String accountType,
        BigDecimal balance,
        BigDecimal interestRate,
        LocalDateTime openedAt,
        BigDecimal minimumBalance,
        BigDecimal monthlyFee,
        BigDecimal monthTotalIn,
        BigDecimal monthTotalOut,
        boolean primary
) {}

