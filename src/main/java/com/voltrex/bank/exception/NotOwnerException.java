package com.voltrex.bank.exception;

public class NotOwnerException extends TransferException {
    public NotOwnerException(String message) { super(message); }
}
