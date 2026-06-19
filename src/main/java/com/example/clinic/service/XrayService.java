package com.example.clinic.service;

import com.example.clinic.dto.request.XrayRequest;
import com.example.clinic.dto.response.XrayResponse;
import com.example.clinic.entity.Appointment;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.Patient;
import com.example.clinic.entity.Xray;
import com.example.clinic.exception.BusinessRuleException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.mapper.XrayMapper;
import com.example.clinic.repository.AppointmentRepository;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.PatientRepository;
import com.example.clinic.repository.XrayRepository;
import com.example.clinic.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class XrayService {

    private final XrayRepository xrayRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final FileStorageService fileStorageService;
    private final XrayMapper xrayMapper;
    private final SecurityUtils securityUtils;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    // ── UPLOAD ────────────────────────────────────────────────────
    @Transactional
    public XrayResponse upload(MultipartFile file, XrayRequest request) {

        // Verify patient exists and is not deleted
        Patient patient = patientRepository.findById(request.patientId())
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + request.patientId()));

        // Verify doctor exists
        Doctor doctor = securityUtils.getLoggedInDoctor();


        // Verify appointment if provided
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

        // Save file to disk — get back the relative path
        String filePath = fileStorageService.saveFile(file,
                request.patientId());

        // Save xray record to database
        Xray xray = Xray.builder()
                .patient(patient)
                .doctor(doctor)
                .appointment(appointment)
                .filePath(filePath)
                .fileName(file.getOriginalFilename())
                .toothNumber(request.toothNumber())
                .description(request.description())
                .takenAt(LocalDateTime.now())
                .build();

        Xray saved = xrayRepository.save(xray);
        log.info("Xray saved with id: {} for patientId: {}",
                saved.getId(), request.patientId());

        return xrayMapper.toResponse(saved, baseUrl);
    }

    // ── GET ALL FOR PATIENT ───────────────────────────────────────
    @Transactional(readOnly = true)
    public List<XrayResponse> getByPatientId(Long patientId) {
        log.debug("Fetching xrays for patientId: {}", patientId);

        patientRepository.findById(patientId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + patientId));

        return xrayRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(x -> xrayMapper.toResponse(x, baseUrl))
                .toList();
    }

    // ── GET FILE (stream the actual image) ────────────────────────
    @Transactional(readOnly = true)
    public Resource getFile(Long xrayId) {
        log.debug("Serving file for xrayId: {}", xrayId);

        Xray xray = findXrayById(xrayId);
        return fileStorageService.loadFile(xray.getFilePath());
    }

    // ── UPDATE METADATA ───────────────────────────────────────────
    @Transactional
    public XrayResponse update(Long xrayId,
                               String description,
                               String toothNumber) {
        log.info("Updating xray id: {}", xrayId);
        Xray xray = findXrayById(xrayId);
        Doctor loggedInDoctor = securityUtils.getLoggedInDoctor();

        if (!xray.getDoctor().getId().equals(loggedInDoctor.getId())) {
            log.warn("Doctor id: {} attempted to edit xray id: {} "
                            + "owned by doctor id: {}",
                    loggedInDoctor.getId(), xrayId,
                    xray.getDoctor().getId());
            throw new BusinessRuleException(
                    "You can only edit your own x-rays");
        }

        xray.setDescription(description);
        xray.setToothNumber(toothNumber);
        Xray updated = xrayRepository.save(xray);

        log.info("Xray id: {} updated", xrayId);
        return xrayMapper.toResponse(updated, baseUrl);
    }

    // ── DELETE ────────────────────────────────────────────────────
    @Transactional
    public void delete(Long xrayId) {
        log.info("Deleting xray id: {}", xrayId);
        Xray xray = findXrayById(xrayId);
        Doctor loggedInDoctor = securityUtils.getLoggedInDoctor();

        if (!xray.getDoctor().getId().equals(loggedInDoctor.getId())) {
            log.warn("Doctor id: {} attempted to delete xray id: {} "
                            + "owned by doctor id: {}",
                    loggedInDoctor.getId(), xrayId,
                    xray.getDoctor().getId());
            throw new BusinessRuleException(
                    "You can only delete your own x-rays");
        }

        fileStorageService.deleteFile(xray.getFilePath());
        xrayRepository.delete(xray);
        log.info("Xray id: {} deleted", xrayId);
    }

    // ── PRIVATE HELPER ────────────────────────────────────────────
    private Xray findXrayById(Long id) {
        return xrayRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "X-ray not found with id: " + id));
    }
}