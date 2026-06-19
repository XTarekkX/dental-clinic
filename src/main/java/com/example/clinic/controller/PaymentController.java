package com.example.clinic.controller;

import com.example.clinic.dto.request.PaymentRequest;
import com.example.clinic.dto.response.PaymentResponse;
import com.example.clinic.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // POST /api/v1/payments
    @PostMapping
    public ResponseEntity<PaymentResponse> create(
            @Valid @RequestBody PaymentRequest request) {

        PaymentResponse response = paymentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/v1/payments/invoice/{invoiceId}
    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<List<PaymentResponse>> getByInvoice(
            @PathVariable Long invoiceId) {

        List<PaymentResponse> response =
                paymentService.getByInvoiceId(invoiceId);
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/payments/patient/{patientId}
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PaymentResponse>> getByPatient(
            @PathVariable Long patientId) {

        List<PaymentResponse> response =
                paymentService.getByPatientId(patientId);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/v1/payments/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        paymentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}