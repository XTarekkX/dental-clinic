package com.example.clinic.exception;

public class InvoiceNotEditableException extends RuntimeException {
    public InvoiceNotEditableException(String message) {
        super(message);
    }
}
