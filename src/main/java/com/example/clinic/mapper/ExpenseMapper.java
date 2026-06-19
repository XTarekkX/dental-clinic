package com.example.clinic.mapper;

import com.example.clinic.dto.response.ExpenseResponse;
import com.example.clinic.entity.Expense;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getDoctor().getId(),
                expense.getDoctor().getFirstName()
                        + " " + expense.getDoctor().getLastName(),
                expense.getCategory(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getCreatedAt()
        );
    }
}