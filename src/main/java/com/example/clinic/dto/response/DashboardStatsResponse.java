package com.example.clinic.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardStatsResponse(

        // Today's appointment breakdown
        long todayTotal,
        long todayOpen,
        long todayCheckedIn,
        long todayInProgress,
        long todayCompleted,
        long todayCancelled,

        // Clinic totals
        long totalActivePatients,
        long totalAppointmentsAllTime,

        // Today's revenue — sum of payments recorded today
        BigDecimal todayRevenue,

        // Next upcoming appointments — for the dashboard list
        List<AppointmentResponse> upcomingAppointments,

        // Recently added patients
        List<PatientResponse> recentPatients
) {}