package com.example.clinic.mapper;

import com.example.clinic.dto.response.AppointmentResponse;
import com.example.clinic.entity.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),

                appointment.getPatient().getId(),
                appointment.getPatient().getFirstName()
                        + " " + appointment.getPatient().getLastName(),

                appointment.getDoctor().getId(),
                appointment.getDoctor().getFirstName()
                        + " " + appointment.getDoctor().getLastName(),

                appointment.getScheduledAt(),
                appointment.getDurationMinutes(),

                // Calculate when appointment ends
                appointment.getScheduledAt()
                        .plusMinutes(appointment.getDurationMinutes()),

                appointment.getStatus(),
                appointment.getReason(),
                appointment.getNotes(),

                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}