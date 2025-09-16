package com.voltrex.bank.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class UserApprovedEvent {
    private String email;
    private String name;
    private String crn;
    private String tempPassword;
    private String accNumber;
}
