package com.voltrex.bank.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {
    private boolean success;
    private String message;
    private String transactionReference;
}
