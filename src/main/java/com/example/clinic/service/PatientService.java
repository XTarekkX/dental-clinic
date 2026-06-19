package com.example.clinic.service;

import com.example.clinic.dto.request.PatientRequest;
import com.example.clinic.dto.response.PatientResponse;
import com.example.clinic.entity.Patient;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.mapper.PatientMapper;
import com.example.clinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    // ── CREATE ────────────────────────────────────────────────────
    @Transactional
    public PatientResponse create(PatientRequest request) {
        log.info("Creating new patient: {} {}",
                request.firstName(), request.lastName());

        // 1. Convert the incoming DTO into a Patient entity
        Patient patient = patientMapper.toEntity(request);

        // 2. Save it to the database
        // Hibernate auto-sets createdAt and updatedAt via BaseEntity
        Patient saved = patientRepository.save(patient);

        // 3. Convert the saved entity into a response DTO and return
        log.info("Patient created with id: {}", saved.getId());
        return patientMapper.toResponse(saved);
    }

    // ── GET ALL (with search and pagination) ──────────────────────
    // Pagination means: instead of returning all 500 patients at once,
    // we return 20 at a time. The frontend asks for page 0, page 1, etc.
    @Transactional(readOnly = true)
    public Page<PatientResponse> getAll(int page, int size, String search) {


        // Sort by lastName alphabetically — feels natural in a clinic list
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("lastName").ascending());

        Page<Patient> patients;

        // If the doctor typed something in the search box, filter by it
        // Otherwise return all active (non-deleted) patients
        if (search != null && !search.isBlank()) {
            patients = patientRepository.searchPatients(search, pageable);
        } else {
            patients = patientRepository.findAllActive(pageable);
        }

        // Convert each Patient entity in the page to a PatientResponse
        // .map() applies our mapper to every item in the page
        return patients.map(patientMapper::toResponse);
    }

    // ── GET ONE BY ID ─────────────────────────────────────────────
    @Transactional(readOnly = true)
    public PatientResponse getById(Long id) {
        log.debug("Fetching patient with id: {}", id);
        Patient patient = findActivePatientById(id);
        return patientMapper.toResponse(patient);
    }

    // ── UPDATE ────────────────────────────────────────────────────
    @Transactional
    public PatientResponse update(Long id, PatientRequest request) {
        log.info("Updating patient with id: {}", id);
        // 1. Find the existing patient — throw 404 if not found
        Patient patient = findActivePatientById(id);

        // 2. Update each field manually
        // We do NOT create a new entity — we update the existing one
        // so the id, createdAt, and history are preserved
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setAddress(request.address());
        patient.setBloodType(request.bloodType());
        patient.setAllergies(request.allergies());
        patient.setEmergencyContact(request.emergencyContact());
        patient.setEmergencyPhone(request.emergencyPhone());

        // 3. Save — Hibernate detects the changes and runs UPDATE SQL
        // updatedAt is automatically refreshed by BaseEntity auditing
        Patient updated = patientRepository.save(patient);
        log.info("Patient updated with id: {}", id);
        return patientMapper.toResponse(updated);
    }

    // ── SOFT DELETE ───────────────────────────────────────────────
    // We never hard-delete patients because they have medical and
    // financial records attached. Soft delete just sets deletedAt.
    @Transactional
    public void delete(Long id) {
        log.info("Soft deleting patient with id: {}", id);
        Patient patient = findActivePatientById(id);

        // Setting deletedAt marks them as deleted without removing the row
        patient.setDeletedAt(LocalDateTime.now());
        patientRepository.save(patient);
        log.info("Patient soft deleted with id: {}", id);
    }

    // ── PRIVATE HELPER ────────────────────────────────────────────
    // Used internally by getById, update, and delete
    // Keeps the "find or throw" logic in one place
    private Patient findActivePatientById(Long id) {
        return patientRepository.findById(id)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + id));
    }
}