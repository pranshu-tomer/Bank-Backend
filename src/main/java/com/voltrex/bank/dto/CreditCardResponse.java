package com.voltrex.bank.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CreditCardResponse(
        Long id,
        String cardNumber,
        LocalDateTime issuedAt,
        boolean active,
        BigDecimal creditLimit,
        LocalDate expiryDate,
        Long userId,
        String userName,
        BigDecimal creditUsed
) {}

