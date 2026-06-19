package com.example.clinic.mapper;

import com.example.clinic.dto.response.OperationResponse;
import com.example.clinic.entity.Operation;
import org.springframework.stereotype.Component;

@Component
public class OperationMapper {

    public OperationResponse toResponse(Operation operation) {
        return new OperationResponse(
                operation.getId(),

                operation.getPatient().getId(),

                operation.getDoctor().getId(),
                operation.getDoctor().getFirstName()
                        + " " + operation.getDoctor().getLastName(),

                operation.getAppointment() != null
                        ? operation.getAppointment().getId() : null,

                operation.getOperationType(),
                operation.getDentalProblem(),
                operation.getTreatmentPlan(),
                operation.getToothNumber(),

                operation.getStatus(),
                operation.getNotes(),

                operation.getPerformedAt(),
                operation.getCreatedAt(),
                operation.getUpdatedAt()
        );
    }
}