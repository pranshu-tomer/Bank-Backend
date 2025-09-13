package com.voltrex.bank.utils;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CardNumberGenerator {

    private final SecureRandom random = new SecureRandom();

    /**
     * Generates a 16-digit credit card number string.
     * Format: 4 + 15 random digits (so it looks like Visa-style)
     */
    public String generate16Digit() {
        StringBuilder sb = new StringBuilder("4"); // prefix for readability
        for (int i = 0; i < 15; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}

