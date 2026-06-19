package com.example.clinic.controller;

import com.example.clinic.dto.request.PatientRequest;
import com.example.clinic.dto.response.*;
import com.example.clinic.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final NoteService noteService;
    private final XrayService xrayService;
    private final OperationService operationService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    // POST /api/v1/patients
    // Creates a new patient
    // Returns 201 Created (not 200) — this is the correct HTTP status for creation
    @PostMapping
    public ResponseEntity<PatientResponse> create(
            @Valid @RequestBody PatientRequest request) {

        PatientResponse response = patientService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/v1/patients?page=0&size=20&search=ahmed
    // Returns a paginated list — search is optional
    @GetMapping
    public ResponseEntity<Page<PatientResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {

        Page<PatientResponse> response = patientService.getAll(page, size, search);
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/patients/{id}
    // Returns one patient's full profile
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getById(@PathVariable Long id) {
        PatientResponse response = patientService.getById(id);
        return ResponseEntity.ok(response);
    }

    // PUT /api/v1/patients/{id}
    // Replaces all patient fields with the new values
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {

        PatientResponse response = patientService.update(id, request);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/v1/patients/{id}
    // Soft deletes — sets deletedAt, does not remove the row
    // Returns 204 No Content — correct status when there is nothing to return
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        patientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/v1/patients/{id}/appointments
    @GetMapping("/{id}/appointments")
    public ResponseEntity<List<AppointmentResponse>> getPatientAppointments(
            @PathVariable Long id) {

        List<AppointmentResponse> response =
                appointmentService.getByPatientId(id);
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/patients/{id}/notes
    @GetMapping("/{id}/notes")
    public ResponseEntity<List<NoteResponse>> getPatientNotes(
            @PathVariable Long id) {

        List<NoteResponse> response = noteService.getByPatientId(id);
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/patients/{id}/xrays
    @GetMapping("/{id}/xrays")
    public ResponseEntity<List<XrayResponse>> getPatientXrays(
            @PathVariable Long id) {

        List<XrayResponse> response = xrayService.getByPatientId(id);
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/patients/{id}/operations
    @GetMapping("/{id}/operations")
    public ResponseEntity<List<OperationResponse>> getPatientOperations(
            @PathVariable Long id) {

        List<OperationResponse> response =
                operationService.getByPatientId(id);
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/patients/{id}/invoices
    @GetMapping("/{id}/invoices")
    public ResponseEntity<List<InvoiceResponse>> getPatientInvoices(
            @PathVariable Long id) {

        List<InvoiceResponse> response = invoiceService.getByPatientId(id);
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/patients/{id}/payments
    @GetMapping("/{id}/payments")
    public ResponseEntity<List<PaymentResponse>> getPatientPayments(
            @PathVariable Long id) {

        List<PaymentResponse> response = paymentService.getByPatientId(id);
        return ResponseEntity.ok(response);
    }
}