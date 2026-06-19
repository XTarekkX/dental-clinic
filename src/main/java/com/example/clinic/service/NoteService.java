package com.example.clinic.service;

import com.example.clinic.dto.request.NoteRequest;
import com.example.clinic.dto.response.NoteResponse;
import com.example.clinic.entity.Appointment;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.Patient;
import com.example.clinic.entity.PatientNote;
import com.example.clinic.exception.BusinessRuleException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.mapper.NoteMapper;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.PatientNoteRepository;
import com.example.clinic.repository.PatientRepository;
import com.example.clinic.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {

    private final PatientNoteRepository noteRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final NoteMapper noteMapper;
    private final SecurityUtils securityUtils;

    // ── CREATE ────────────────────────────────────────────────────
    @Transactional
    public NoteResponse create(NoteRequest request) {
        log.info("Creating note for patientId: {}", request.patientId());

        Patient patient = patientRepository.findById(request.patientId())
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + request.patientId()));

        // Get the logged-in doctor automatically from the JWT
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

        PatientNote note = PatientNote.builder()
                .patient(patient)
                .doctor(doctor)
                .appointment(appointment)
                .content(request.content())
                .build();

        PatientNote saved = noteRepository.save(note);
        log.info("Note created with id: {} for patientId: {}",
                saved.getId(), request.patientId());

        return noteMapper.toResponse(saved);
    }


    // ── GET ALL FOR PATIENT ───────────────────────────────────────
    @Transactional(readOnly = true)
    public List<NoteResponse> getByPatientId(Long patientId) {
        log.debug("Fetching notes for patientId: {}", patientId);

        // Verify patient exists
        patientRepository.findById(patientId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + patientId));

        return noteRepository
                .findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(noteMapper::toResponse)
                .toList();
    }

    // ── UPDATE ────────────────────────────────────────────────────
    @Transactional
    public NoteResponse update(Long noteId, String newContent) {
        log.info("Updating note id: {}", noteId);

        PatientNote note = findNoteById(noteId);
        Doctor loggedInDoctor = securityUtils.getLoggedInDoctor();

        if (!note.getDoctor().getId().equals(loggedInDoctor.getId())) {
            log.warn("Doctor id: {} attempted to edit note id: {} "
                            + "owned by doctor id: {}",
                    loggedInDoctor.getId(), noteId,
                    note.getDoctor().getId());
            throw new BusinessRuleException(
                    "You can only edit your own notes");
        }

        note.setContent(newContent);
        PatientNote updated = noteRepository.save(note);

        log.info("Note id: {} updated successfully", noteId);
        return noteMapper.toResponse(updated);
    }

    // ── DELETE ────────────────────────────────────────────────────
    @Transactional
    public void delete(Long noteId) {
        log.info("Deleting note id: {}", noteId);

        PatientNote note = findNoteById(noteId);
        Doctor loggedInDoctor = securityUtils.getLoggedInDoctor();

        if (!note.getDoctor().getId().equals(loggedInDoctor.getId())) {
            log.warn("Doctor id: {} attempted to delete note id: {} "
                            + "owned by doctor id: {}",
                    loggedInDoctor.getId(), noteId,
                    note.getDoctor().getId());
            throw new BusinessRuleException(
                    "You can only delete your own notes");
        }

        noteRepository.delete(note);
        log.info("Note id: {} deleted successfully", noteId);
    }

    // ── PRIVATE HELPER ────────────────────────────────────────────
    private PatientNote findNoteById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Note not found with id: " + id));
    }
}