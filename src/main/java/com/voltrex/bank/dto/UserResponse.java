package com.voltrex.bank.dto;

import com.voltrex.bank.entities.Address;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private String crn;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private Address address;
    private boolean tfa;
    private boolean loginAlert;

}
