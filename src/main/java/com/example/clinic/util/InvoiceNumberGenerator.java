package com.example.clinic.util;

import com.example.clinic.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class InvoiceNumberGenerator {

    private final InvoiceRepository invoiceRepository;

    // Generates a unique, human-readable invoice number
    // Format: INV-2024-00042
    // The number part is total invoices count + 1, zero-padded to 5 digits
    public String generate() {
        long count = invoiceRepository.count();
        int year = LocalDate.now().getYear();
        String number = String.format("%05d", count + 1);
        return "INV-" + year + "-" + number;
    }
}