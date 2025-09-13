package com.voltrex.bank.dto;

import lombok.Data;

@Data
public class UserResponse {
    private String crn;
    private String name;
    private String email;
    private String phone;
}
