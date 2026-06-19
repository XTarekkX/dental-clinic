package com.example.clinic.service;

import com.example.clinic.dto.request.ExpenseRequest;
import com.example.clinic.dto.response.ExpenseResponse;
import com.example.clinic.entity.Doctor;
import com.example.clinic.entity.Expense;
import com.example.clinic.exception.BusinessRuleException;
import com.example.clinic.exception.ResourceNotFoundException;
import com.example.clinic.mapper.ExpenseMapper;
import com.example.clinic.repository.DoctorRepository;
import com.example.clinic.repository.ExpenseRepository;
import com.example.clinic.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final DoctorRepository doctorRepository;
    private final ExpenseMapper expenseMapper;
    private final SecurityUtils securityUtils;

    // ── CREATE ────────────────────────────────────────────────────
    @Transactional
    public ExpenseResponse create(ExpenseRequest request) {

        Doctor doctor = securityUtils.getLoggedInDoctor();

        Expense expense = Expense.builder()
                .doctor(doctor)
                .category(request.category())
                .description(request.description())
                .amount(request.amount())
                .expenseDate(request.expenseDate())
                .build();

        Expense saved = expenseRepository.save(expense);
        log.info("Expense created with id: {}", saved.getId());

        return expenseMapper.toResponse(saved);
    }

    // ── GET ALL (paginated, filtered by date range) ───────────────
    @Transactional(readOnly = true)
    public Page<ExpenseResponse> getAll(Long doctorId,
                                        LocalDate startDate,
                                        LocalDate endDate,
                                        int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("expenseDate").descending());

        return expenseRepository
                .findByDoctorIdAndExpenseDateBetween(
                        doctorId, startDate, endDate, pageable)
                .map(expenseMapper::toResponse);
    }

    // ── UPDATE ────────────────────────────────────────────────────
    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest request) {
        log.info("Updating expense id: {}", id);

        Expense expense = findExpenseById(id);
        Doctor loggedInDoctor = securityUtils.getLoggedInDoctor();

        if (!expense.getDoctor().getId().equals(loggedInDoctor.getId())) {
            throw new BusinessRuleException(
                    "You can only edit your own expenses");
        }

        expense.setCategory(request.category());
        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setExpenseDate(request.expenseDate());

        Expense updated = expenseRepository.save(expense);
        log.info("Expense id: {} updated", id);
        return expenseMapper.toResponse(updated);
    }

    // ── DELETE ────────────────────────────────────────────────────
    public void delete(Long id) {
        log.info("Deleting expense id: {}", id);

        Expense expense = findExpenseById(id);
        Doctor loggedInDoctor = securityUtils.getLoggedInDoctor();

        if (!expense.getDoctor().getId().equals(loggedInDoctor.getId())) {
            throw new BusinessRuleException(
                    "You can only delete your own expenses");
        }

        expenseRepository.delete(expense);
        log.info("Expense id: {} deleted", id);
    }

    // ── PRIVATE HELPER ────────────────────────────────────────────
    private Expense findExpenseById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expense not found with id: " + id));
    }
}