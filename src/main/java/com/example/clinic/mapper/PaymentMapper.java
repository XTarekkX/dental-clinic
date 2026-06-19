package com.example.clinic.mapper;

import com.example.clinic.dto.response.PaymentResponse;
import com.example.clinic.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),

                payment.getInvoice().getId(),
                payment.getInvoice().getInvoiceNumber(),

                payment.getPatient().getId(),
                payment.getPatient().getFirstName()
                        + " " + payment.getPatient().getLastName(),

                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getReferenceNumber(),

                payment.getPaidAt(),
                payment.getNotes(),

                payment.getCreatedAt()
        );
    }
}