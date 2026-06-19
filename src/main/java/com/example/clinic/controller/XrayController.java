package com.example.clinic.controller;

import com.example.clinic.dto.request.XrayRequest;
import com.example.clinic.dto.response.XrayResponse;
import com.example.clinic.service.XrayService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/xrays")
@RequiredArgsConstructor
public class XrayController {

    private final XrayService xrayService;

    // POST /api/v1/xrays
    // multipart/form-data — file + metadata fields together
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<XrayResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart("patientId") String patientId,
            @RequestPart(value = "appointmentId", required = false)
            String appointmentId,
            @RequestPart(value = "toothNumber", required = false)
            String toothNumber,
            @RequestPart(value = "description", required = false)
            String description) {

        XrayRequest request = new XrayRequest(
                Long.parseLong(patientId),
                appointmentId != null ? Long.parseLong(appointmentId) : null,
                toothNumber,
                description
        );

        XrayResponse response = xrayService.upload(file, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/v1/xrays/patient/{patientId}
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<XrayResponse>> getByPatient(
            @PathVariable Long patientId) {

        List<XrayResponse> response = xrayService.getByPatientId(patientId);
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/xrays/{id}/file
    // Streams the actual image file — used by the frontend to display it
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getFile(@PathVariable Long id) {

        Resource resource = xrayService.getFile(id);

        return ResponseEntity.ok()
                // Tell the browser to display inline (not download)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + resource.getFilename() + "\"")
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    // PUT /api/v1/xrays/{id}?doctorId=1
    @PutMapping("/{id}")
    public ResponseEntity<XrayResponse> update(
            @PathVariable Long id,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String toothNumber) {

        XrayResponse response = xrayService.update(id, description, toothNumber);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/v1/xrays/{id}?doctorId=1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        xrayService.delete(id);
        return ResponseEntity.noContent().build();
    }
}