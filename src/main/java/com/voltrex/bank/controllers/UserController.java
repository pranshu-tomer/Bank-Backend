package com.voltrex.bank.controllers;

import com.voltrex.bank.dto.AccountResponse;
import com.voltrex.bank.dto.UserResponse;
import com.voltrex.bank.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {


    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getMyAccounts() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }
        UserResponse response = new UserResponse();
        response.setName(currentUser.getFirstName()+" "+currentUser.getLastName());
        response.setEmail(currentUser.getEmail());
        response.setPhone(currentUser.getPhone());
        response.setCrn(currentUser.getCrn());
        return ResponseEntity.ok(Map.of("success", true, "user", response));
    }

}
