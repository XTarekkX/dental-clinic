package com.example.clinic.controller;

import com.example.clinic.dto.request.NoteRequest;
import com.example.clinic.dto.response.NoteResponse;
import com.example.clinic.service.NoteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    // POST /api/v1/notes
    @PostMapping
    public ResponseEntity<NoteResponse> create(
            @Valid @RequestBody NoteRequest request) {
        NoteResponse response = noteService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/v1/notes/patient/{patientId}
    // Returns all notes for a patient — newest first
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<NoteResponse>> getByPatient(
            @PathVariable Long patientId) {

        List<NoteResponse> response =
                noteService.getByPatientId(patientId);
        return ResponseEntity.ok(response);
    }

    // PUT /api/v1/notes/{id}?doctorId=1
    // Only the doctor who wrote the note can edit it
    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> update(
            @PathVariable Long id,
            @RequestBody @NotBlank String content) {
        NoteResponse response = noteService.update(id, content);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/v1/notes/{id}?doctorId=1
    // Only the doctor who wrote the note can delete it
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}