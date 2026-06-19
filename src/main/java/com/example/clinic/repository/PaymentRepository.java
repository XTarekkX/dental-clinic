package com.example.clinic.repository;

import com.example.clinic.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoiceId(Long invoiceId);

    List<Payment> findByPatientId(Long patientId);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
            WHERE p.invoice.id = :invoiceId
            """)
    BigDecimal sumPaymentsByInvoiceId(@Param("invoiceId") Long invoiceId);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
            WHERE p.invoice.doctor.id = :doctorId
            AND p.paidAt BETWEEN :start AND :end
            """)
    BigDecimal sumPaymentsByDoctorAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}