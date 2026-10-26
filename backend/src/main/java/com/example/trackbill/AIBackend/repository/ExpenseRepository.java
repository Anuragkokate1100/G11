package com.example.trackbill.AIBackend.repository;

import com.example.trackbill.AIBackend.model.Expense;
import com.example.trackbill.AIBackend.model.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByTractorOwnerIdOrderByExpenseDateDesc(Long ownerId);

    Optional<Expense> findByIdAndTractorOwnerId(Long id, Long ownerId);

    List<Expense> findByTractorOwnerIdAndExpenseDateBetweenOrderByExpenseDateDesc(Long ownerId, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.tractorOwner.id = :ownerId")
    BigDecimal sumAmountByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.tractorOwner.id = :ownerId AND e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumAmountByOwnerIdAndExpenseDateBetween(@Param("ownerId") Long ownerId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.tractorOwner.id = :ownerId AND e.expenseType = :type")
    BigDecimal sumAmountByOwnerIdAndExpenseType(@Param("ownerId") Long ownerId, @Param("type") ExpenseType type);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.tractorOwner.id = :ownerId AND e.expenseType = :type AND e.expenseDate BETWEEN :start AND :end")
    BigDecimal sumAmountByOwnerIdAndExpenseTypeAndExpenseDateBetween(@Param("ownerId") Long ownerId, @Param("type") ExpenseType type, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT e FROM Expense e WHERE e.tractorOwner.id = :ownerId AND " +
           "(LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(CAST(e.expenseType AS string)) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY e.expenseDate DESC")
    List<Expense> searchExpenses(@Param("ownerId") Long ownerId, @Param("query") String query);

    @Query("SELECT e.expenseType, SUM(e.amount) FROM Expense e WHERE e.tractorOwner.id = :ownerId GROUP BY e.expenseType")
    List<Object[]> getCategoryExpenseBreakdown(@Param("ownerId") Long ownerId);

    @Query("SELECT e.expenseType, SUM(e.amount) FROM Expense e WHERE e.tractorOwner.id = :ownerId AND e.expenseDate BETWEEN :start AND :end GROUP BY e.expenseType")
    List<Object[]> getCategoryExpenseBreakdownBetween(@Param("ownerId") Long ownerId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT FUNCTION('YEAR', e.expenseDate), FUNCTION('MONTH', e.expenseDate), SUM(e.amount) " +
           "FROM Expense e WHERE e.tractorOwner.id = :ownerId " +
           "GROUP BY FUNCTION('YEAR', e.expenseDate), FUNCTION('MONTH', e.expenseDate) " +
           "ORDER BY FUNCTION('YEAR', e.expenseDate) ASC, FUNCTION('MONTH', e.expenseDate) ASC")
    List<Object[]> getMonthlyExpenseTrends(@Param("ownerId") Long ownerId);
}
