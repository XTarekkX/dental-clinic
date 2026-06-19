package com.example.clinic.service;

import com.example.clinic.exception.BusinessRuleException;
import com.example.clinic.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    @Value("${app.file.max-size-mb}")
    private int maxSizeMb;

    @Value("${app.file.allowed-types}")
    private String allowedTypes;

    // ── SAVE FILE ─────────────────────────────────────────────────
    // Saves the uploaded file to disk under a patient-specific folder
    // Returns the relative path stored in the database
    public String saveFile(MultipartFile file, Long patientId) {
        log.info("Saving file for patientId: {} originalName: {}",
                patientId, file.getOriginalFilename());

        // 1. Validate the file
        validateFile(file);

        try {
            // 2. Create the directory for this patient if it doesn't exist
            // Structure: uploads/xrays/patient_5/
            Path patientDir = Paths.get(uploadDir, "patient_" + patientId);
            Files.createDirectories(patientDir);

            // 3. Generate a unique filename to prevent collisions
            // Even if two doctors upload "scan.jpg" they won't overwrite each other
            String extension = getExtension(file.getOriginalFilename());
            String uniqueFileName = UUID.randomUUID() + "." + extension;

            // 4. Save the file to disk
            Path targetPath = patientDir.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetPath,
                    StandardCopyOption.REPLACE_EXISTING);

            // 5. Return the relative path — this is what we store in the DB
            String relativePath = "patient_" + patientId + "/" + uniqueFileName;
            log.info("File saved successfully at: {}", relativePath);

            return relativePath;

        } catch (IOException ex) {
            log.error("Failed to save file for patientId: {}", patientId, ex);
            throw new BusinessRuleException(
                    "Could not save file: " + ex.getMessage());
        }
    }

    // ── LOAD FILE ─────────────────────────────────────────────────
    // Reads the file from disk and returns it as a Spring Resource
    // The controller streams this Resource back to the frontend
    public Resource loadFile(String filePath) {
        log.debug("Loading file: {}", filePath);

        try {
            Path fullPath = Paths.get(uploadDir).resolve(filePath);
            Resource resource = new UrlResource(fullPath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                log.warn("File not found or not readable: {}", filePath);
                throw new ResourceNotFoundException(
                        "File not found: " + filePath);
            }

            return resource;

        } catch (MalformedURLException ex) {
            log.error("Malformed file path: {}", filePath, ex);
            throw new ResourceNotFoundException("File not found: " + filePath);
        }
    }

    // ── DELETE FILE ───────────────────────────────────────────────
    public void deleteFile(String filePath) {
        log.info("Deleting file: {}", filePath);

        try {
            Path fullPath = Paths.get(uploadDir).resolve(filePath);
            boolean deleted = Files.deleteIfExists(fullPath);

            if (deleted) {
                log.info("File deleted: {}", filePath);
            } else {
                log.warn("File not found for deletion: {}", filePath);
            }

        } catch (IOException ex) {
            log.error("Failed to delete file: {}", filePath, ex);
            // We log but do not throw — if file is already gone, that is fine
        }
    }

    // ── PRIVATE: validate file ────────────────────────────────────
    private void validateFile(MultipartFile file) {

        // Empty file check
        if (file.isEmpty()) {
            throw new BusinessRuleException("Cannot upload an empty file");
        }

        // Size check
        long maxBytes = (long) maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessRuleException(
                    "File size exceeds maximum allowed size of "
                            + maxSizeMb + "MB");
        }

        // Type check — only allow safe image formats
        List<String> allowed = Arrays.asList(allowedTypes.split(","));
        String contentType = file.getContentType();
        if (contentType == null || !allowed.contains(contentType)) {
            throw new BusinessRuleException(
                    "File type not allowed: " + contentType
                            + ". Allowed types: " + allowedTypes);
        }
    }

    // ── PRIVATE: get file extension ───────────────────────────────
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg"; // safe default
        }
        return filename.substring(filename.lastIndexOf('.') + 1)
                .toLowerCase();
    }
}