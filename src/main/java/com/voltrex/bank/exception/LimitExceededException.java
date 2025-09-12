package com.voltrex.bank.exception;

public class LimitExceededException extends TransferException {
    public LimitExceededException(String message) { super(message); }
}
