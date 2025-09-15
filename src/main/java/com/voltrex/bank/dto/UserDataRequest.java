package com.voltrex.bank.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDataRequest {

    @NotBlank
    private String firstName;
    private String lastName;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String phone;

    @NotBlank
    private String street;
    @NotBlank
    private String city;
    @NotBlank
    private String state;
    @NotBlank
    private String pincode;
}
