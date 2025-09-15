package com.voltrex.bank.controllers;

import com.voltrex.bank.dto.TransactionResponse;
import com.voltrex.bank.entities.User;
import com.voltrex.bank.services.TransactionService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * GET /api/transactions?from=2025-09-01T00:00:00&to=2025-09-12T23:59:59&page=0&size=20
     *
     * If `from`/`to` are omitted, defaults to last 30 days.
     */
    @GetMapping("")
    public ResponseEntity<?> getTransactions(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,

            @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(value = "size", defaultValue = "25") @Min(1) int size,
            @RequestParam(value = "sortBy", defaultValue = "executedAt") String sortBy,
            @RequestParam(value = "dir", defaultValue = "DESC") String dir
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof User)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "error", "Unauthorized"));
        }
        User current = (User) principal;
        long userId = current.getId();

        // defaults
        LocalDateTime now = LocalDateTime.now();
        if (to == null) to = now;
        if (from == null) from = now.minusDays(30); // last 30 days

        Sort.Direction direction = "ASC".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Page<TransactionResponse> result = transactionService.getTransactionsForUser(userId, from, to, page, size, sortBy, direction);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "page", result.getNumber(),
                "size", result.getSize(),
                "totalPages", result.getTotalPages(),
                "totalElements", result.getTotalElements(),
                "transactions", result.getContent()
        ));
    }
}
