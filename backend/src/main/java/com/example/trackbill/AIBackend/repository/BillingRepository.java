package com.example.trackbill.AIBackend.repository;

import com.example.trackbill.AIBackend.model.Billing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillingRepository extends JpaRepository<Billing, Long> {

    @Query("SELECT b FROM Billing b JOIN FETCH b.tractorOwner WHERE b.tractorOwner.id = :ownerId ORDER BY b.billDate DESC")
    List<Billing> findByTractorOwnerIdOrderByBillDateDesc(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Billing b JOIN FETCH b.tractorOwner WHERE b.id = :id AND b.tractorOwner.id = :ownerId")
    Optional<Billing> findByIdAndTractorOwnerId(@Param("id") Long id, @Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Billing b JOIN FETCH b.tractorOwner WHERE b.tractorOwner.id = :ownerId AND b.billDate BETWEEN :start AND :end ORDER BY b.billDate DESC")
    List<Billing> findByTractorOwnerIdAndBillDateBetweenOrderByBillDateDesc(@Param("ownerId") Long ownerId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Billing b WHERE b.tractorOwner.id = :ownerId")
    BigDecimal sumRevenueByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Billing b WHERE b.tractorOwner.id = :ownerId AND b.billDate BETWEEN :start AND :end")
    BigDecimal sumRevenueByOwnerIdAndBillDateBetween(@Param("ownerId") Long ownerId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT b FROM Billing b JOIN FETCH b.tractorOwner WHERE b.tractorOwner.id = :ownerId AND (" +
           "LOWER(b.sugarcaneOwnerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "CAST(b.id AS string) LIKE CONCAT('%', :query, '%')) " +
           "ORDER BY b.billDate DESC")
    List<Billing> searchBills(@Param("ownerId") Long ownerId, @Param("query") String query);

    @Query("SELECT FUNCTION('YEAR', b.billDate), FUNCTION('MONTH', b.billDate), SUM(b.totalAmount) " +
           "FROM Billing b WHERE b.tractorOwner.id = :ownerId " +
           "GROUP BY FUNCTION('YEAR', b.billDate), FUNCTION('MONTH', b.billDate) " +
           "ORDER BY FUNCTION('YEAR', b.billDate) ASC, FUNCTION('MONTH', b.billDate) ASC")
    List<Object[]> getMonthlyRevenueTrends(@Param("ownerId") Long ownerId);
}
