package com.voltrex.bank.controllers;

import com.voltrex.bank.dto.TransferByAccountRequest;
import com.voltrex.bank.dto.TransferByReceiverRequest;
import com.voltrex.bank.dto.TransferResponse;
import com.voltrex.bank.entities.User;
import com.voltrex.bank.services.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/account")
    public ResponseEntity<TransferResponse> transferByAccount(@Valid @RequestBody TransferByAccountRequest req) {
        // get current authenticated user (your JwtFilter places User as principal)
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TransferResponse(false, "Unauthorized", null));
        }
        User currentUser = (User) principal;

        try {
            String ref = transferService.transferByAccount(req, currentUser);
            return ResponseEntity.ok(new TransferResponse(true, "Transfer completed", ref));
        } catch (Exception ex) {
            // Let GlobalExceptionHandler map specific exceptions to proper responses.
            // Fallback: return bad request.
            return ResponseEntity.badRequest().body(new TransferResponse(false, ex.getMessage(), null));
        }
    }

    @PostMapping("/receiver")
    public ResponseEntity<TransferResponse> transferByReceiver(@Valid @RequestBody TransferByReceiverRequest req) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TransferResponse(false, "Unauthorized", null));
        }
        User currentUser = (User) principal;

        try {
            String ref = transferService.transferByReceiver(req, currentUser);
            return ResponseEntity.ok(new TransferResponse(true, "Transfer completed", ref));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new TransferResponse(false, ex.getMessage(), null));
        }
    }
}
