package com.example.clinic.controller;

import com.example.clinic.dto.request.OperationRequest;
import com.example.clinic.dto.response.OperationResponse;
import com.example.clinic.enums.OperationStatus;
import com.example.clinic.service.OperationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/operations")
@RequiredArgsConstructor
public class OperationController {

    private final OperationService operationService;

    // POST /api/v1/operations
    @PostMapping
    public ResponseEntity<OperationResponse> create(
            @Valid @RequestBody OperationRequest request) {

        OperationResponse response = operationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/v1/operations/patient/{patientId}
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<OperationResponse>> getByPatient(
            @PathVariable Long patientId) {

        List<OperationResponse> response =
                operationService.getByPatientId(patientId);
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/operations/{id}
    @GetMapping("/{id}")
    public ResponseEntity<OperationResponse> getById(
            @PathVariable Long id) {

        OperationResponse response = operationService.getById(id);
        return ResponseEntity.ok(response);
    }

    // PUT /api/v1/operations/{id}?doctorId=1
    @PutMapping("/{id}")
    public ResponseEntity<OperationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody OperationRequest request) {

        OperationResponse response = operationService.update(id, request);
        return ResponseEntity.ok(response);
    }

    // PATCH /api/v1/operations/{id}/status?doctorId=1&status=COMPLETED
    @PatchMapping("/{id}/status")
    public ResponseEntity<OperationResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam @NotNull OperationStatus status) {

        OperationResponse response =
                operationService.updateStatus(id, status);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/v1/operations/{id}?doctorId=1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        operationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}