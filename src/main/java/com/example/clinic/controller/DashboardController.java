package com.example.clinic.controller;

import com.example.clinic.dto.response.DashboardStatsResponse;
import com.example.clinic.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // GET /api/v1/dashboard/stats
    // Returns everything the dashboard page needs in one call
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        DashboardStatsResponse response = dashboardService.getStats();
        return ResponseEntity.ok(response);
    }
}