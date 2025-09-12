package com.voltrex.bank.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ReferenceGenerator {

    /**
     * Generates a unique transaction reference.
     * Currently uses UUID without dashes (32 chars).
     * Can be replaced later with shorter numeric codes if required.
     */
    public String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

