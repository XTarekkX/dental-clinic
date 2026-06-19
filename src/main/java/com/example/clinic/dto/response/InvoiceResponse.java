package com.example.clinic.dto.response;

import com.example.clinic.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record InvoiceResponse(

        Long id,
        String invoiceNumber,

        Long patientId,
        String patientFullName,

        Long doctorId,
        String doctorFullName,

        Long appointmentId,

        List<InvoiceItemResponse> items,

        BigDecimal totalAmount,
        BigDecimal discount,
        BigDecimal tax,
        BigDecimal netAmount,

        // How much has been paid so far
        BigDecimal amountPaid,

        // How much is still owed
        BigDecimal balanceDue,

        InvoiceStatus status,
        LocalDate dueDate,
        String notes,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}