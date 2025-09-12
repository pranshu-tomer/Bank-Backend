package com.voltrex.bank.utils;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class CRNGenerator {
    public String generate() {
        // simple short CRN: remove dashes, take first 10 chars
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}

