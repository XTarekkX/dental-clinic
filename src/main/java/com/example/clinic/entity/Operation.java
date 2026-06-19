package com.example.clinic.entity;

import com.example.clinic.enums.OperationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "operations")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Operation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(length = 100)
    private String operationType;

    @Column(columnDefinition = "TEXT")
    private String dentalProblem;

    @Column(columnDefinition = "TEXT")
    private String treatmentPlan;

    @Column(length = 10)
    private String toothNumber;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OperationStatus status = OperationStatus.PLANNED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime performedAt;
}