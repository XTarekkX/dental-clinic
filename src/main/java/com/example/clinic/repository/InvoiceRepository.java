package com.example.clinic.repository;

import com.example.clinic.entity.Invoice;
import com.example.clinic.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByPatientId(Long patientId);

    Page<Invoice> findByDoctorId(Long doctorId, Pageable pageable);

    Page<Invoice> findByDoctorIdAndStatus(Long doctorId, InvoiceStatus status, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(i.netAmount), 0) FROM Invoice i
        WHERE i.doctor.id = :doctorId
        AND i.status IN :statuses
        AND i.createdAt BETWEEN :start AND :end
        """)
    BigDecimal sumPaidInvoicesByDoctorAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("statuses") List<InvoiceStatus> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i
        WHERE i.doctor.id = :doctorId
        AND i.status != 'CANCELLED'
        AND i.createdAt BETWEEN :start AND :end
        """)
    BigDecimal sumTotalInvoicedByDoctorAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT COUNT(i) FROM Invoice i
        WHERE i.doctor.id = :doctorId
        AND i.status = :status
        AND i.createdAt BETWEEN :start AND :end
        """)
    long countByDoctorAndStatusAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("status") InvoiceStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}