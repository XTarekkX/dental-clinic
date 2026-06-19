package com.example.clinic.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "xrays")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Xray extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(nullable = false, length = 500)
    private String filePath;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 10)
    private String toothNumber;

    private LocalDateTime takenAt;
}