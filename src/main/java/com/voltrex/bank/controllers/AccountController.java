package com.voltrex.bank.controllers;

import com.voltrex.bank.dto.AccountResponse;
import com.voltrex.bank.dto.CreditCardResponse;
import com.voltrex.bank.entities.User;
import com.voltrex.bank.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountQueryService;

    @GetMapping("/me/accounts")
    public ResponseEntity<Map<String, Object>> getMyAccounts() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }

        List<AccountResponse> accounts = accountQueryService.getAccountsForUser(userId);
        return ResponseEntity.ok(Map.of("success", true, "accounts", accounts));
    }

//    @GetMapping("/me/credit-card")
//    public ResponseEntity<Map<String, Object>> getMyCreditCard() {
//        Long userId = SecurityUtils.getCurrentUserId();
//        if (userId == null) {
//            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
//        }
//
//        CreditCardResponse cc = accountQueryService.getCreditCardForUser(userId);
//        if (cc == null) {
//            return ResponseEntity.ok(Map.of("success", true, "creditCard", null));
//        }
//        return ResponseEntity.ok(Map.of("success", true, "creditCard", cc));
//    }
}

