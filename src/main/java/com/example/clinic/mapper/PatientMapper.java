package com.example.clinic.mapper;

import com.example.clinic.dto.request.PatientRequest;
import com.example.clinic.dto.response.PatientResponse;
import com.example.clinic.entity.Patient;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

@Component
public class PatientMapper {

    // Turns a PatientRequest (from frontend) into a Patient entity (for database)
    public Patient toEntity(PatientRequest request) {
        return Patient.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender())
                .phone(request.phone())
                .email(request.email())
                .address(request.address())
                .bloodType(request.bloodType())
                .allergies(request.allergies())
                .emergencyContact(request.emergencyContact())
                .emergencyPhone(request.emergencyPhone())
                .build();
    }

    // Turns a Patient entity (from database) into a PatientResponse (for frontend)
    public PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                calculateAge(patient.getDateOfBirth()),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getPhone(),
                patient.getEmail(),
                patient.getAddress(),
                patient.getBloodType(),
                patient.getAllergies(),
                patient.getEmergencyContact(),
                patient.getEmergencyPhone(),
                patient.getCreatedAt(),
                patient.getUpdatedAt()
        );
    }

    // Helper — calculates age from date of birth
    // Returns null if dateOfBirth was not provided
    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return null;
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }
}