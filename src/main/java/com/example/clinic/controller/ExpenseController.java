package com.example.clinic.controller;

import com.example.clinic.dto.request.ExpenseRequest;
import com.example.clinic.dto.response.ExpenseResponse;
import com.example.clinic.service.ExpenseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    // POST /api/v1/expenses
    @PostMapping
    public ResponseEntity<ExpenseResponse> create(
            @Valid @RequestBody ExpenseRequest request) {

        ExpenseResponse response = expenseService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/v1/expenses?doctorId=1&startDate=2024-01-01&endDate=2024-12-31
    @GetMapping
    public ResponseEntity<Page<ExpenseResponse>> getAll(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ExpenseResponse> response = expenseService.getAll(
                doctorId, startDate, endDate, page, size);
        return ResponseEntity.ok(response);
    }

    // PUT /api/v1/expenses/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request) {

        ExpenseResponse response = expenseService.update(id, request);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/v1/expenses/{id}?doctorId=1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}