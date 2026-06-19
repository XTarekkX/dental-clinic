package com.example.clinic.repository;

import com.example.clinic.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("""
            SELECT p FROM Patient p
            WHERE p.deletedAt IS NULL
            AND (
                LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(p.phone) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(p.email) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            """)
    Page<Patient> searchPatients(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Patient p WHERE p.deletedAt IS NULL")
    Page<Patient> findAllActive(Pageable pageable);

    @Query("""
        SELECT COUNT(p) FROM Patient p
        WHERE p.deletedAt IS NULL
        AND p.createdAt BETWEEN :start AND :end
        """)
    long countNewPatientsBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.deletedAt IS NULL")
    long countAllActive();
}