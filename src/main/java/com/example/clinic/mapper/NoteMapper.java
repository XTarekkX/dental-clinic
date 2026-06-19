package com.example.clinic.mapper;

import com.example.clinic.dto.response.NoteResponse;
import com.example.clinic.entity.PatientNote;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

    public NoteResponse toResponse(PatientNote note) {
        return new NoteResponse(
                note.getId(),

                note.getPatient().getId(),

                note.getDoctor().getId(),
                note.getDoctor().getFirstName()
                        + " " + note.getDoctor().getLastName(),

                // appointment is optional — could be null
                note.getAppointment() != null
                        ? note.getAppointment().getId() : null,
                note.getAppointment() != null
                        ? note.getAppointment().getScheduledAt() : null,

                note.getContent(),

                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}