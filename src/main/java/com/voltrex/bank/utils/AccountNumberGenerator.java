package com.voltrex.bank.utils;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class AccountNumberGenerator {
    private final SecureRandom rnd = new SecureRandom();

    /**
     * Generate a 12-digit numeric string. Collisions should be checked by caller.
     */
    public String generate12Digit() {
        long part = Math.abs(rnd.nextLong()) % 1_000_000_000_000L; // 0..999,999,999,999
        return String.format("%012d", part);
    }
}

