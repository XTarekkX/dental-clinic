package com.example.clinic.repository;

import com.example.clinic.entity.Operation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {

    List<Operation> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    @Query("""
        SELECT o FROM Operation o
        WHERE o.doctor.id = :doctorId
        AND o.createdAt BETWEEN :start AND :end
        """)
    List<Operation> findByDoctorIdAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}