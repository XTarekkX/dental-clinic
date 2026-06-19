package com.example.clinic.mapper;

import com.example.clinic.dto.response.InvoiceItemResponse;
import com.example.clinic.dto.response.InvoiceResponse;
import com.example.clinic.entity.Invoice;
import com.example.clinic.entity.InvoiceItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class InvoiceMapper {

    public InvoiceResponse toResponse(Invoice invoice,
                                      BigDecimal amountPaid) {
        List<InvoiceItemResponse> itemResponses = invoice.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal balanceDue = invoice.getNetAmount()
                .subtract(amountPaid);

        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),

                invoice.getPatient().getId(),
                invoice.getPatient().getFirstName()
                        + " " + invoice.getPatient().getLastName(),

                invoice.getDoctor().getId(),
                invoice.getDoctor().getFirstName()
                        + " " + invoice.getDoctor().getLastName(),

                invoice.getAppointment() != null
                        ? invoice.getAppointment().getId() : null,

                itemResponses,

                invoice.getTotalAmount(),
                invoice.getDiscount(),
                invoice.getTax(),
                invoice.getNetAmount(),

                amountPaid,
                balanceDue,

                invoice.getStatus(),
                invoice.getDueDate(),
                invoice.getNotes(),

                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }

    private InvoiceItemResponse toItemResponse(InvoiceItem item) {
        return new InvoiceItemResponse(
                item.getId(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice()
        );
    }
}