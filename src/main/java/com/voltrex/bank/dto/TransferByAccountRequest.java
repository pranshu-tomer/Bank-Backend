package com.voltrex.bank.dto;

import com.voltrex.bank.entities.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferByAccountRequest {
    @NotBlank
    private String fromAccountNumber;

    @NotBlank
    private String toAccountNumber;

    @NotBlank
    private String toAccountName; // used for receiver name verification

    @NotNull
    @Positive
    private BigDecimal amount;

    private String description;

    @NotNull
    private TransactionType type;
}


