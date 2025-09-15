package com.voltrex.bank.dto;

import com.voltrex.bank.entities.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private Long id;
    private String referenceNumber;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private String status;
    private LocalDateTime executedAt;

    private String fromAccountNumber;
    private String toAccountNumber;

    // new fields:
    private String senderName;      // fromAccount.owner full name (or null)
    private String receiverName;    // toAccount.owner full name (or null)
    private String direction;       // "DEBIT" | "CREDIT" | "INTERNAL"

    private BigDecimal fromAccountBalanceAfter; // optional
    private BigDecimal toAccountBalanceAfter;   // optional
}
