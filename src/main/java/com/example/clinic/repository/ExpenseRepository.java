package com.example.clinic.repository;

import com.example.clinic.dto.response.ExpenseResponse;
import com.example.clinic.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // Replace the existing list method with this paginated version
    Page<Expense> findByDoctorIdAndExpenseDateBetween(
            Long doctorId,
            LocalDate start,
            LocalDate end,
            Pageable pageable
    );

    @Query("""
            SELECT COALESCE(SUM(e.amount), 0) FROM Expense e
            WHERE e.doctor.id = :doctorId
            AND e.expenseDate BETWEEN :start AND :end
            """)
    BigDecimal sumExpensesByDoctorAndDateRange(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
}