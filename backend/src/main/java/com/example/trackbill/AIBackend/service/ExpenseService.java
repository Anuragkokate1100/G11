package com.example.trackbill.AIBackend.service;

import com.example.trackbill.AIBackend.dto.ExpenseRequest;
import com.example.trackbill.AIBackend.dto.ExpenseResponse;
import com.example.trackbill.AIBackend.exception.BadRequestException;
import com.example.trackbill.AIBackend.exception.ResourceNotFoundException;
import com.example.trackbill.AIBackend.model.Expense;
import com.example.trackbill.AIBackend.model.TractorOwner;
import com.example.trackbill.AIBackend.repository.ExpenseRepository;
import com.example.trackbill.AIBackend.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<ExpenseResponse> getAllExpenses() {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        return expenseRepository.findByTractorOwnerIdOrderByExpenseDateDesc(ownerId)
                .stream()
                .map(ExpenseResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ExpenseResponse getExpenseById(Long id) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        Expense expense = expenseRepository.findByIdAndTractorOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense record not found with ID: " + id));
        return ExpenseResponse.fromEntity(expense);
    }

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        TractorOwner currentOwner = SecurityUtil.getCurrentOwner();

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Expense amount cannot be negative");
        }

        Expense expense = new Expense();
        expense.setExpenseType(request.getExpenseType());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
        expense.setTractorOwner(currentOwner);

        Expense saved = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(saved);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        Expense expense = expenseRepository.findByIdAndTractorOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense record not found with ID: " + id));

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Expense amount cannot be negative");
        }

        expense.setExpenseType(request.getExpenseType());
        expense.setAmount(request.getAmount());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");

        Expense updated = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteExpense(Long id) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        Expense expense = expenseRepository.findByIdAndTractorOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense record not found with ID: " + id));

        expenseRepository.delete(expense);
    }

    public List<ExpenseResponse> searchExpenses(String query) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        if (query == null || query.isBlank()) {
            return getAllExpenses();
        }
        return expenseRepository.searchExpenses(ownerId, query.trim())
                .stream()
                .map(ExpenseResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ExpenseResponse> filterExpensesByDate(LocalDate start, LocalDate end) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        if (start == null && end == null) {
            return getAllExpenses();
        }
        if (start == null) start = LocalDate.of(2000, 1, 1);
        if (end == null) end = LocalDate.now();

        return expenseRepository.findByTractorOwnerIdAndExpenseDateBetweenOrderByExpenseDateDesc(ownerId, start, end)
                .stream()
                .map(ExpenseResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
