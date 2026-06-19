package com.example.clinic.controller;

import com.example.clinic.dto.request.InvoiceRequest;
import com.example.clinic.dto.response.InvoiceResponse;
import com.example.clinic.enums.InvoiceStatus;
import com.example.clinic.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    // POST /api/v1/invoices
    @PostMapping
    public ResponseEntity<InvoiceResponse> create(
            @Valid @RequestBody InvoiceRequest request) {

        InvoiceResponse response = invoiceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/v1/invoices?doctorId=1&page=0&size=20&status=DRAFT
    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) InvoiceStatus status) {

        Page<InvoiceResponse> response =
                invoiceService.getAll(page, size, status);
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/invoices/{id}
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getById(
            @PathVariable Long id) {

        InvoiceResponse response = invoiceService.getById(id);
        return ResponseEntity.ok(response);
    }

    // PUT /api/v1/invoices/{id}
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceRequest request) {

        InvoiceResponse response = invoiceService.update(id, request);
        return ResponseEntity.ok(response);
    }

    // PATCH /api/v1/invoices/{id}/status?status=ISSUED
    @PatchMapping("/{id}/status")
    public ResponseEntity<InvoiceResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam InvoiceStatus status) {

        InvoiceResponse response = invoiceService.updateStatus(id, status);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/v1/invoices/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        invoiceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}