package com.voltrex.bank.utils;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class PasswordGenerator {
    private static final String ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
    private final SecureRandom rnd = new SecureRandom();

    public String generate(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUM.charAt(rnd.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }
}
