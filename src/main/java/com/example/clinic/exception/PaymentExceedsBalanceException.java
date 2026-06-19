package com.example.clinic.exception;

public class PaymentExceedsBalanceException extends RuntimeException {
    public PaymentExceedsBalanceException(String message) {
        super(message);
    }
}
