package com.voltrex.bank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", ex.getMessage()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "errors", errors
        ));
    }

    @ExceptionHandler(NotOwnerException.class)
    public ResponseEntity<Map<String,Object>> handleNotOwner(NotOwnerException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "error", ex.getMessage()));
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<Map<String,Object>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "error", ex.getMessage()));
    }

    @ExceptionHandler({InsufficientFundsException.class, LimitExceededException.class, TransferException.class})
    public ResponseEntity<Map<String,Object>> handleBadRequest(TransferException ex) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,Object>> handleAll(Exception ex) {
        // log error server-side in real app (omitted here)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "error", "Internal server error"));
    }
}









