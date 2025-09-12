package com.voltrex.bank.exception;

public class NotFoundException extends TransferException {
    public NotFoundException(String message) { super(message); }
}
