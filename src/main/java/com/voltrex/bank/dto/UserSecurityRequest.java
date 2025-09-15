package com.voltrex.bank.dto;

import lombok.Data;

@Data
public class UserSecurityRequest {
    private boolean tfa;
    private boolean loginAlert;
}
