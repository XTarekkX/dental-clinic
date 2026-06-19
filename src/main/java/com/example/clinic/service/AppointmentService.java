package com.example.clinic.service;

import com.example.clinic.dto.request.AppointmentRequest;
import com.example.clinic.dto.response.AppointmentResponse;
import com.example.clinic.entity.Appointment;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.Patient;
import com.example.clinic.enums.AppointmentStatus;
import com.example.clinic.exception.BusinessRuleException;
import com.example.clinic.exception.ConflictException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.mapper.AppointmentMapper;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.PatientRepository;
import com.example.clinic.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentMapper appointmentMapper;
    private final SecurityUtils securityUtils;

    // ── CREATE ────────────────────────────────────────────────────
    @Transactional
    public AppointmentResponse create(AppointmentRequest request) {

        // 1. Load patient — throw 404 if not found or soft deleted
        Patient patient = patientRepository.findById(request.patientId())
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + request.patientId()));

        Doctor doctor = securityUtils.getLoggedInDoctor();

        // 3. Calculate the end time of the new appointment
        int duration = request.durationMinutes() != null
                ? request.durationMinutes() : 30;
        LocalDateTime newStart = request.scheduledAt();
        LocalDateTime newEnd = newStart.plusMinutes(duration);

        // 4. Check for scheduling conflicts
        checkForConflicts(doctor.getId(), newStart, newEnd, null);

        // 5. Build the entity
        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .scheduledAt(newStart)
                .durationMinutes(duration)
                .status(AppointmentStatus.OPEN)
                .reason(request.reason())
                .build();

        Appointment saved = appointmentRepository.save(appointment);
        log.info("Appointment created with id: {}", saved.getId());

        return appointmentMapper.toResponse(saved);
    }

    // ── GET ALL (for calendar — filtered by doctor and date range) ─
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getByDateRange(
            LocalDateTime start,
            LocalDateTime end) {

        log.debug("Fetching all appointments between {} and {}", start, end);

        return appointmentRepository
                .findByScheduledAtBetween(start, end)
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    // ── GET ALL FOR A PATIENT ─────────────────────────────────────
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getByPatientId(Long patientId) {
        log.debug("Fetching appointments for patientId: {}", patientId);

        // Verify patient exists first
        patientRepository.findById(patientId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + patientId));

        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    // ── GET TODAY'S APPOINTMENTS FOR A DOCTOR ────────────────────
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getTodayForDoctor(Long doctorId) {
        log.debug("Fetching today's appointments for doctorId: {}", doctorId);

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return appointmentRepository
                .findByDoctorIdAndScheduledAtBetween(doctorId, startOfDay, endOfDay)
                .stream()
                .map(appointmentMapper::toResponse)
                .toList();
    }

    // ── GET ONE BY ID ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public AppointmentResponse getById(Long id) {
        log.debug("Fetching appointment with id: {}", id);
        return appointmentMapper.toResponse(findAppointmentById(id));
    }

    // ── UPDATE ────────────────────────────────────────────────────
    @Transactional
    public AppointmentResponse update(Long id, AppointmentRequest request) {
        log.info("Updating appointment with id: {}", id);

        Appointment appointment = findAppointmentById(id);

        // Cannot edit a completed or cancelled appointment
        if (appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.CANCELLED
                || appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new BusinessRuleException(
                    "Cannot edit an appointment with status: "
                            + appointment.getStatus());
        }

        // If time is being changed — re-check for conflicts
        // Pass current id to exclude this appointment from the check
        int duration = request.durationMinutes() != null
                ? request.durationMinutes() : appointment.getDurationMinutes();
        LocalDateTime newStart = request.scheduledAt();
        LocalDateTime newEnd = newStart.plusMinutes(duration);

        checkForConflicts(appointment.getDoctor().getId(),
                newStart, newEnd, id);

        appointment.setScheduledAt(newStart);
        appointment.setDurationMinutes(duration);
        appointment.setReason(request.reason());
        appointment.setNotes(appointment.getNotes());

        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment updated with id: {}", id);

        return appointmentMapper.toResponse(updated);
    }

    // ── UPDATE STATUS ONLY ────────────────────────────────────────
    // This is the most-used operation — the doctor clicks
    // "Check In", "Start", "Complete" on the calendar
    @Transactional
    public AppointmentResponse updateStatus(Long id, AppointmentStatus newStatus) {
        log.info("Updating status of appointment id: {} to {}", id, newStatus);

        Appointment appointment = findAppointmentById(id);

        // Enforce valid transitions
        validateStatusTransition(appointment.getStatus(), newStatus);

        appointment.setStatus(newStatus);
        Appointment updated = appointmentRepository.save(appointment);

        log.info("Appointment id: {} status changed to {}", id, newStatus);
        return appointmentMapper.toResponse(updated);
    }

    // ── DELETE ────────────────────────────────────────────────────
    @Transactional
    public void delete(Long id) {
        log.info("Cancelling appointment with id: {}", id);

        Appointment appointment = findAppointmentById(id);

        // Cannot cancel a completed appointment
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Cannot cancel a completed appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);

        log.info("Appointment id: {} cancelled", id);
    }

    // ── PRIVATE: find or throw ────────────────────────────────────
    private Appointment findAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with id: " + id));
    }

    // ── PRIVATE: conflict detection ───────────────────────────────
    // Checks if the doctor has any overlapping appointment in this time slot
    // excludeId is used during update so we don't flag the appointment itself
    private void checkForConflicts(Long doctorId,
                                   LocalDateTime newStart,
                                   LocalDateTime newEnd,
                                   Long excludeId) {

        List<Appointment> existing = appointmentRepository
                .findByDoctorIdAndScheduledAtBetween(
                        doctorId,
                        newStart.minusHours(12),
                        newEnd.plusHours(12));

        for (Appointment a : existing) {

            // Skip the appointment being updated
            if (excludeId != null && a.getId().equals(excludeId)) continue;

            // Skip cancelled and no-show appointments
            if (a.getStatus() == AppointmentStatus.CANCELLED
                    || a.getStatus() == AppointmentStatus.NO_SHOW) continue;

            LocalDateTime existingEnd = a.getScheduledAt()
                    .plusMinutes(a.getDurationMinutes());

            // Standard overlap formula
            boolean overlaps = newStart.isBefore(existingEnd)
                    && newEnd.isAfter(a.getScheduledAt());

            if (overlaps) {
                log.warn("Conflict detected for doctorId: {} at {}",
                        doctorId, newStart);
                throw new ConflictException(
                        "Doctor already has an appointment from "
                                + a.getScheduledAt()
                                + " to " + existingEnd);
            }
        }
    }

    // ── PRIVATE: status transition rules ─────────────────────────
    private void validateStatusTransition(AppointmentStatus current,
                                          AppointmentStatus next) {
        boolean valid = switch (current) {
            case OPEN -> next == AppointmentStatus.CHECKED_IN
                    || next == AppointmentStatus.CANCELLED
                    || next == AppointmentStatus.NO_SHOW;

            case CHECKED_IN -> next == AppointmentStatus.IN_PROGRESS
                    || next == AppointmentStatus.CANCELLED;

            case IN_PROGRESS -> next == AppointmentStatus.COMPLETED;

            // Terminal states — no transitions allowed out of these
            case COMPLETED, CANCELLED, NO_SHOW -> false;
        };

        if (!valid) {
            log.warn("Invalid status transition attempted: {} → {}", current, next);
            throw new BusinessRuleException(
                    "Cannot transition appointment from "
                            + current + " to " + next);
        }
    }
}