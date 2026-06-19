package com.example.clinic.dto.response;

import java.time.LocalDate;

public record PatientReportResponse(

        LocalDate startDate,
        LocalDate endDate,

        long newPatientsInPeriod,
        long totalActivePatients
) {}