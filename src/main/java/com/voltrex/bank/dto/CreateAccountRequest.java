package com.voltrex.bank.dto;

import com.voltrex.bank.entities.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAccountRequest {

    @NotNull
    private String type; // SAVINGS, SALARY, CURRENT
}

