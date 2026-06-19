package com.example.clinic.service;

import com.example.clinic.dto.request.OperationRequest;
import com.example.clinic.dto.response.OperationResponse;
import com.example.clinic.entity.Appointment;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.Operation;
import com.example.clinic.entity.Patient;
import com.example.clinic.enums.OperationStatus;
import com.example.clinic.exception.BusinessRuleException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.mapper.OperationMapper;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.OperationRepository;
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
public class OperationService {

    private final OperationRepository operationRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final OperationMapper operationMapper;
    private final SecurityUtils securityUtils;

    // ── CREATE ────────────────────────────────────────────────────
    @Transactional
    public OperationResponse create(OperationRequest request) {

        Patient patient = patientRepository.findById(request.patientId())
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + request.patientId()));

        Doctor doctor = securityUtils.getLoggedInDoctor();

        Appointment appointment = null;
        if (request.appointmentId() != null) {
            appointment = appointmentRepository
                    .findById(request.appointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Appointment not found with id: "
                                    + request.appointmentId()));

            if (!appointment.getPatient().getId()
                    .equals(request.patientId())) {
                throw new BusinessRuleException(
                        "Appointment does not belong to this patient");
            }
        }

        // Default status is PLANNED if not provided
        OperationStatus status = request.status() != null
                ? request.status() : OperationStatus.PLANNED;

        // If status is COMPLETED, set performedAt automatically
        LocalDateTime performedAt = status == OperationStatus.COMPLETED
                ? LocalDateTime.now() : null;

        Operation operation = Operation.builder()
                .patient(patient)
                .doctor(doctor)
                .appointment(appointment)
                .operationType(request.operationType())
                .dentalProblem(request.dentalProblem())
                .treatmentPlan(request.treatmentPlan())
                .toothNumber(request.toothNumber())
                .status(status)
                .notes(request.notes())
                .performedAt(performedAt)
                .build();

        Operation saved = operationRepository.save(operation);
        log.info("Operation created with id: {} status: {}",
                saved.getId(), saved.getStatus());

        return operationMapper.toResponse(saved);
    }

    // ── GET ALL FOR PATIENT ───────────────────────────────────────
    @Transactional(readOnly = true)
    public List<OperationResponse> getByPatientId(Long patientId) {
        log.debug("Fetching operations for patientId: {}", patientId);

        patientRepository.findById(patientId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + patientId));

        return operationRepository
                .findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(operationMapper::toResponse)
                .toList();
    }

    // ── GET ONE ───────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public OperationResponse getById(Long id) {
        log.debug("Fetching operation with id: {}", id);
        return operationMapper.toResponse(findOperationById(id));
    }

    // ── UPDATE ────────────────────────────────────────────────────
    @Transactional
    public OperationResponse update(Long operationId,
                                    OperationRequest request) {
        log.info("Updating operation id: {}", operationId);

        Operation operation = findOperationById(operationId);
        Doctor loggedInDoctor = securityUtils.getLoggedInDoctor();

        if (!operation.getDoctor().getId().equals(loggedInDoctor.getId())) {
            log.warn("Doctor id: {} attempted to edit operation id: {} "
                            + "owned by doctor id: {}",
                    loggedInDoctor.getId(), operationId,
                    operation.getDoctor().getId());

            throw new BusinessRuleException(
                    "You can only edit your own operations");
        }

        if (operation.getStatus() == OperationStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Cannot edit a completed operation");
        }

        if (!operation.getPatient().getId().equals(request.patientId())) {
            throw new BusinessRuleException(
                    "Operation patient cannot be changed");
        }

        Appointment appointment = null;

        if (request.appointmentId() != null) {
            appointment = appointmentRepository
                    .findById(request.appointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Appointment not found with id: "
                                    + request.appointmentId()));

            if (!appointment.getPatient().getId()
                    .equals(operation.getPatient().getId())) {
                throw new BusinessRuleException(
                        "Appointment does not belong to this patient");
            }
        }

        operation.setAppointment(appointment);
        operation.setOperationType(request.operationType());
        operation.setDentalProblem(request.dentalProblem());
        operation.setTreatmentPlan(request.treatmentPlan());
        operation.setToothNumber(request.toothNumber());
        operation.setNotes(request.notes());

        Operation updated = operationRepository.save(operation);

        log.info("Operation id: {} updated", operationId);

        return operationMapper.toResponse(updated);
    }

    // ── UPDATE STATUS ─────────────────────────────────────────────
    @Transactional
    public OperationResponse updateStatus(Long operationId,
                                          OperationStatus newStatus) {
        log.info("Updating status of operation id: {} to {}",
                operationId, newStatus);

        Operation operation = findOperationById(operationId);
        Doctor loggedInDoctor = securityUtils.getLoggedInDoctor();

        if (!operation.getDoctor().getId().equals(loggedInDoctor.getId())) {
            throw new BusinessRuleException(
                    "You can only update status of your own operations");
        }

        validateStatusTransition(operation.getStatus(), newStatus);
        operation.setStatus(newStatus);

        if (newStatus == OperationStatus.COMPLETED) {
            operation.setPerformedAt(LocalDateTime.now());
        }

        Operation updated = operationRepository.save(operation);
        log.info("Operation id: {} status changed to {}", operationId, newStatus);
        return operationMapper.toResponse(updated);
    }

    // ── DELETE ────────────────────────────────────────────────────
    @Transactional
    public void delete(Long operationId) {
        log.info("Deleting operation id: {}", operationId);
        Operation operation = findOperationById(operationId);
        Doctor loggedInDoctor = securityUtils.getLoggedInDoctor();

        if (!operation.getDoctor().getId().equals(loggedInDoctor.getId())) {
            log.warn("Doctor id: {} attempted to delete operation id: {} "
                            + "owned by doctor id: {}",
                    loggedInDoctor.getId(), operationId,
                    operation.getDoctor().getId());
            throw new BusinessRuleException(
                    "You can only delete your own operations");
        }

        if (operation.getStatus() == OperationStatus.COMPLETED) {
            throw new BusinessRuleException(
                    "Cannot delete a completed operation. "
                            + "Medical records must be preserved.");
        }

        operationRepository.delete(operation);
        log.info("Operation id: {} deleted", operationId);
    }

    // ── PRIVATE: find or throw ────────────────────────────────────
    private Operation findOperationById(Long id) {
        return operationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Operation not found with id: " + id));
    }

    // ── PRIVATE: status transitions ───────────────────────────────
    private void validateStatusTransition(OperationStatus current,
                                          OperationStatus next) {
        boolean valid = switch (current) {
            case PLANNED -> next == OperationStatus.IN_PROGRESS
                    || next == OperationStatus.COMPLETED;
            case IN_PROGRESS -> next == OperationStatus.COMPLETED;
            case COMPLETED -> false; // terminal — no transitions out
        };

        if (!valid) {
            log.warn("Invalid operation status transition: {} → {}",
                    current, next);
            throw new BusinessRuleException(
                    "Cannot transition operation from "
                            + current + " to " + next);
        }
    }
}