package com.example.clinic.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "patient_notes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PatientNote extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}