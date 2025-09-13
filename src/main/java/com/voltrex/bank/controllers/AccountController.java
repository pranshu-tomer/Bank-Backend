package com.voltrex.bank.controllers;

import com.voltrex.bank.dto.AccountResponse;
import com.voltrex.bank.dto.CreateAccountRequest;
import com.voltrex.bank.dto.CreditCardResponse;
import com.voltrex.bank.dto.ProductResponse;
import com.voltrex.bank.entities.User;
import com.voltrex.bank.services.AccountService;
import com.voltrex.bank.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final ProductService productService;

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getMyAccounts() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = currentUser.getId();
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }

        List<AccountResponse> accounts = accountService.getAccountsForUser(userId);
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

    @PostMapping("/create")
    public ResponseEntity<Map<String,Object>> createAccount(@Valid @RequestBody CreateAccountRequest req) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User currentUser)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }

        try {
            String accNumber = accountService.createAccount(currentUser, req);
            return ResponseEntity.ok(Map.of("success", true, "accountNumber", accNumber));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
        }
    }

    @GetMapping("/options")
    public ResponseEntity<Map<String,Object>> getAvailableOptions() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            return ResponseEntity.status(401).build();
        }
        User current = (User) principal;
        List<ProductResponse> resp = productService.getAvailableProductsForUser(current.getId());
        return ResponseEntity.ok(Map.of("success",true,"data",resp));
    }
}