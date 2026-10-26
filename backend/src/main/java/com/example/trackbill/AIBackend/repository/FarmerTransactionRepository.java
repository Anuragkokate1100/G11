package com.example.trackbill.AIBackend.repository;

import com.example.trackbill.AIBackend.model.FarmerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

public interface FarmerTransactionRepository extends JpaRepository<FarmerTransaction, Long> {

 List<FarmerTransaction> findAllByFarmerIdOrderByTransactionDateDescIdDesc(Long farmerId);

 Optional<FarmerTransaction> findByIdAndFarmerId(Long id, Long farmerId);

 List<FarmerTransaction> findAllByFarmerIdAndTransactionDateBetweenOrderByTransactionDateDesc(
         Long farmerId, LocalDate start, LocalDate end
 );

 @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FarmerTransaction t " +
         "WHERE t.farmerId = :farmerId " +
         "AND t.transactionType = :type " +
         "AND t.transactionDate BETWEEN :start AND :end")
 BigDecimal sumByFarmerIdAndTransactionTypeAndTransactionDateBetween(
         @Param("farmerId") Long farmerId,
         @Param("type") String type,
         @Param("start") LocalDate start,
         @Param("end") LocalDate end
 );
}
