package com.voltrex.bank.dto;

import com.voltrex.bank.entities.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAccountRequest {

    @NotNull
    private AccountType type; // SAVINGS, SALARY, CURRENT
}

