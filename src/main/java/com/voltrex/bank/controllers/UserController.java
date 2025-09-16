package com.voltrex.bank.controllers;

import com.voltrex.bank.dto.*;
import com.voltrex.bank.entities.Address;
import com.voltrex.bank.entities.User;
import com.voltrex.bank.repositories.UserRepository;
import com.voltrex.bank.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getMyAccounts() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }

        UserResponse response = new UserResponse();
        response.setFirstName(currentUser.getFirstName());
        response.setLastName(currentUser.getLastName());
        response.setEmail(currentUser.getEmail());
        response.setPhone(currentUser.getPhone());
        response.setCrn(currentUser.getCrn());
        response.setCreatedAt(currentUser.getCreatedAt());

        Address address = currentUser.getAddress();
        response.setAddress(address);
        response.setTfa(currentUser.isTwoFactorEnabled());
        response.setLoginAlert(currentUser.isLoginAlert());

        return ResponseEntity.ok(Map.of("success", true, "user", response));
    }

    @PostMapping("/data/save")
    public ResponseEntity<Map<String,Object>> saveUserData(@Valid @RequestBody UserDataRequest req) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }

        return userService.updateUser(req,currentUser);
    }

    @PostMapping("/security/save")
    public ResponseEntity<Map<String,Object>> saveUserData(@RequestBody UserSecurityRequest req) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }

        currentUser.setTwoFactorEnabled(req.isTfa());
        currentUser.setLoginAlert(req.isLoginAlert());

        userRepo.save(currentUser);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody PasswordChangeRequest req) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User currentUser)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "error", "Unauthorized"));
        }

        // Reload managed entity from DB
        User managed = userRepo.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Check old password
        if (!passwordEncoder.matches(req.getOldPassword(), managed.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", "Old password is incorrect"));
        }

        // Encode and save new password
        String encodedNew = passwordEncoder.encode(req.getNewPassword());
        managed.setPassword(encodedNew);
        userRepo.save(managed);

        return ResponseEntity.ok(Map.of("success", true, "message", "Password updated successfully"));
    }
}
