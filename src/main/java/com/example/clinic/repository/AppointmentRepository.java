package com.example.clinic.repository;

import com.example.clinic.entity.Appointment;
import com.example.clinic.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDoctorIdAndScheduledAtBetween(
            Long doctorId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Appointment> findByPatientId(Long patientId);

    @Query("""
            SELECT a FROM Appointment a
            WHERE a.doctor.id = :doctorId
            AND a.scheduledAt >= :start
            AND a.scheduledAt < :end
            AND a.status = :status
            """)
    List<Appointment> findByDoctorIdAndDateRangeAndStatus(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") AppointmentStatus status
    );

    @Query("""
        SELECT a FROM Appointment a
        WHERE a.doctor.id = :doctorId
        AND a.scheduledAt >= :now
        AND a.status = :status
        ORDER BY a.scheduledAt ASC
        """)
    List<Appointment> findUpcomingByDoctorId(
            @Param("doctorId") Long doctorId,
            @Param("now") LocalDateTime now,
            @Param("status") AppointmentStatus status
    );

    @Query("""
        SELECT a FROM Appointment a
        WHERE a.doctor.id = :doctorId
        AND a.scheduledAt >= :start
        AND a.scheduledAt < :end
        """)
    List<Appointment> findByDoctorIdAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<Appointment> findByScheduledAtBetween(
            LocalDateTime start,
            LocalDateTime end
    );
}