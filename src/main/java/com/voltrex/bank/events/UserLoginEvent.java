package com.voltrex.bank.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginEvent {
    private String email;
    private String name;
}
