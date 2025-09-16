package com.voltrex.bank.events;

import com.voltrex.bank.entities.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransactionEvent {
    private String email;
    private String name;
    private String direction;
    private AccountType type;
    private String number;
    private BigDecimal amount;
    private BigDecimal newBalance;
}
