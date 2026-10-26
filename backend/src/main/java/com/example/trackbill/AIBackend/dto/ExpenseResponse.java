package com.example.trackbill.AIBackend.dto;

import com.example.trackbill.AIBackend.model.Expense;
import com.example.trackbill.AIBackend.model.ExpenseType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ExpenseResponse {
    private Long id;
    private ExpenseType expenseType;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String description;
    private LocalDateTime createdAt;

    public ExpenseResponse() {
    }

    public ExpenseResponse(Long id, ExpenseType expenseType, BigDecimal amount, LocalDate expenseDate, String description, LocalDateTime createdAt) {
        this.id = id;
        this.expenseType = expenseType;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.description = description;
        this.createdAt = createdAt;
    }

    public static ExpenseResponse fromEntity(Expense expense) {
        if (expense == null) return null;
        return new ExpenseResponse(
                expense.getId(),
                expense.getExpenseType(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getDescription(),
                expense.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ExpenseType getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(ExpenseType expenseType) {
        this.expenseType = expenseType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
