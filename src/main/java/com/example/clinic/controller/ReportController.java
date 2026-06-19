package com.example.clinic.controller;

import com.example.clinic.dto.response.*;
import com.example.clinic.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // GET /api/v1/reports/financial?startDate=2024-01-01&endDate=2024-12-31
    @GetMapping("/financial")
    public ResponseEntity<FinancialReportResponse> getFinancialReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return ResponseEntity.ok(
                reportService.getFinancialReport(startDate, endDate));
    }

    // GET /api/v1/reports/appointments?startDate=...&endDate=...
    @GetMapping("/appointments")
    public ResponseEntity<AppointmentReportResponse> getAppointmentReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return ResponseEntity.ok(
                reportService.getAppointmentReport(startDate, endDate));
    }

    // GET /api/v1/reports/patients?startDate=...&endDate=...
    @GetMapping("/patients")
    public ResponseEntity<PatientReportResponse> getPatientReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return ResponseEntity.ok(
                reportService.getPatientReport(startDate, endDate));
    }

    // GET /api/v1/reports/operations?startDate=...&endDate=...
    @GetMapping("/operations")
    public ResponseEntity<OperationReportResponse> getOperationReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return ResponseEntity.ok(
                reportService.getOperationReport(startDate, endDate));
    }
}