package com.voltrex.bank.exception;

public class InsufficientFundsException extends TransferException {
    public InsufficientFundsException(String message) { super(message); }
}
