package com.example.clinic.service;

import com.example.clinic.dto.response.AppointmentResponse;
import com.example.clinic.dto.response.DashboardStatsResponse;
import com.example.clinic.dto.response.PatientResponse;
import com.example.clinic.entity.Doctor;
import com.example.clinic.enums.AppointmentStatus;
import com.example.clinic.mapper.AppointmentMapper;
import com.example.clinic.mapper.PatientMapper;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.PatientRepository;
import com.example.clinic.repository.PaymentRepository;
import com.example.clinic.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final PaymentRepository paymentRepository;
    private final AppointmentMapper appointmentMapper;
    private final PatientMapper patientMapper;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        Doctor doctor = securityUtils.getLoggedInDoctor();
        log.debug("Loading dashboard stats for doctorId: {}",
                doctor.getId());

        // Today's date range
        LocalDateTime startOfDay = LocalDateTime.now()
                .toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // Load all of today's appointments for this doctor
        List<AppointmentResponse> todayAppointments =
                appointmentRepository
                        .findByDoctorIdAndScheduledAtBetween(
                                doctor.getId(), startOfDay, endOfDay)
                        .stream()
                        .map(appointmentMapper::toResponse)
                        .toList();

        // Count by status
        long todayTotal = todayAppointments.size();
        long todayOpen = countByStatus(
                todayAppointments, AppointmentStatus.OPEN);
        long todayCheckedIn = countByStatus(
                todayAppointments, AppointmentStatus.CHECKED_IN);
        long todayInProgress = countByStatus(
                todayAppointments, AppointmentStatus.IN_PROGRESS);
        long todayCompleted = countByStatus(
                todayAppointments, AppointmentStatus.COMPLETED);
        long todayCancelled = countByStatus(
                todayAppointments, AppointmentStatus.CANCELLED);

        // Total active patients in the clinic
        long totalActivePatients = patientRepository
                .findAllActive(PageRequest.of(0, 1))
                .getTotalElements();

        // Total appointments ever
        long totalAppointments = appointmentRepository.count();

        // Revenue collected today
        BigDecimal todayRevenue = paymentRepository
                .sumPaymentsByDoctorAndDateRange(
                        doctor.getId(), startOfDay, endOfDay);

        // Next 5 upcoming open appointments
        List<AppointmentResponse> upcoming =
                appointmentRepository.findUpcomingByDoctorId(
                                doctor.getId(),
                                LocalDateTime.now(),
                                AppointmentStatus.OPEN)
                        .stream()
                        .limit(5)
                        .map(appointmentMapper::toResponse)
                        .toList();

        // Last 5 patients created
        List<PatientResponse> recentPatients = patientRepository
                .findAllActive(PageRequest.of(
                        0, 5,
                        Sort.by("createdAt").descending()))
                .getContent()
                .stream()
                .map(patientMapper::toResponse)
                .toList();

        log.debug("Dashboard loaded — today: {} appointments, "
                + "revenue: {}", todayTotal, todayRevenue);

        return new DashboardStatsResponse(
                todayTotal,
                todayOpen,
                todayCheckedIn,
                todayInProgress,
                todayCompleted,
                todayCancelled,
                totalActivePatients,
                totalAppointments,
                todayRevenue,
                upcoming,
                recentPatients
        );
    }

    // ── PRIVATE HELPER ────────────────────────────────────────────
    private long countByStatus(List<AppointmentResponse> appointments,
                               AppointmentStatus status) {
        return appointments.stream()
                .filter(a -> a.status() == status)
                .count();
    }
}