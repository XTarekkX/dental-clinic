package com.example.clinic.controller;

import com.example.clinic.dto.request.AppointmentRequest;
import com.example.clinic.dto.response.AppointmentResponse;
import com.example.clinic.enums.AppointmentStatus;
import com.example.clinic.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // POST /api/v1/appointments
    @PostMapping
    public ResponseEntity<AppointmentResponse> create(
            @Valid @RequestBody AppointmentRequest request) {

        AppointmentResponse response = appointmentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/v1/appointments?doctorId=1&start=2024-01-01T00:00&end=2024-01-31T23:59
    // Used by the calendar to load appointments for a date range
    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end) {

        List<AppointmentResponse> response =
                appointmentService.getByDateRange(start, end);
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/appointments/today?doctorId=1
    // Dashboard and calendar daily view
    @GetMapping("/today")
    public ResponseEntity<List<AppointmentResponse>> getToday(
            @RequestParam Long doctorId) {

        List<AppointmentResponse> response =
                appointmentService.getTodayForDoctor(doctorId);
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/appointments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getById(@PathVariable Long id) {
        AppointmentResponse response = appointmentService.getById(id);
        return ResponseEntity.ok(response);
    }

    // PUT /api/v1/appointments/{id}
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentRequest request) {

        AppointmentResponse response = appointmentService.update(id, request);
        return ResponseEntity.ok(response);
    }

    // PATCH /api/v1/appointments/{id}/status
    // Body: { "status": "CHECKED_IN" }
    // PATCH is used here — not PUT — because we are updating ONE field only
    @PatchMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam AppointmentStatus status) {

        AppointmentResponse response =
                appointmentService.updateStatus(id, status);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/v1/appointments/{id}
    // Does not delete the row — sets status to CANCELLED
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        appointmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}