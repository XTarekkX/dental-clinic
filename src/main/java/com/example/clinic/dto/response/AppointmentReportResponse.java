package com.example.clinic.dto.response;

import java.time.LocalDate;

public record AppointmentReportResponse(

        LocalDate startDate,
        LocalDate endDate,

        long totalAppointments,
        long openAppointments,
        long checkedInAppointments,
        long inProgressAppointments,
        long completedAppointments,
        long cancelledAppointments,
        long noShowAppointments,

        // Completion rate as a percentage
        double completionRate
) {}