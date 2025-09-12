package com.voltrex.bank.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String crn;
    @NotBlank
    private String password;
}
