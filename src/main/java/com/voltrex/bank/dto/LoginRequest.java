package com.voltrex.bank.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String crn;
    private String password;
}
