package com.voltrex.bank.controllers;

import com.voltrex.bank.entities.Account;
import com.voltrex.bank.repositories.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class TestController {

    // make this final so Lombok generates constructor and Spring injects it
    private final AccountRepository accountRepository;

    @PostMapping("/topup/{accountNumber}")
    public ResponseEntity<Map<String, Object>> topUpTest(@PathVariable("accountNumber") String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Account not found"));

        BigDecimal current = account.getBalance();
        if (current == null) current = BigDecimal.ZERO;
        BigDecimal added = BigDecimal.valueOf(10000);
        BigDecimal updated = current.add(added);
        account.setBalance(updated);
        accountRepository.save(account);

        return ResponseEntity.ok(Map.of(
                "accountNumber", account.getAccountNumber(),
                "oldBalance", current,
                "added", added,
                "newBalance", updated
        ));
    }
}
