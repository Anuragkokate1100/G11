package com.example.trackbill.AIBackend.repository;

import com.example.trackbill.AIBackend.model.LabourPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabourPaymentRepository extends JpaRepository<LabourPayment, Long> {

    List<LabourPayment> findByTractorOwnerIdOrderByPaymentDateDesc(Long ownerId);

    Optional<LabourPayment> findByIdAndTractorOwnerId(Long id, Long ownerId);

    List<LabourPayment> findByTractorOwnerIdAndLabourIdOrderByPaymentDateDesc(Long ownerId, Long labourId);

    List<LabourPayment> findByTractorOwnerIdAndBillingId(Long ownerId, Long billingId);

    @Query("SELECT COALESCE(SUM(lp.finalAmount), 0) FROM LabourPayment lp WHERE lp.tractorOwner.id = :ownerId")
    BigDecimal sumFinalAmountByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT COALESCE(SUM(lp.advanceDeducted), 0) FROM LabourPayment lp WHERE lp.tractorOwner.id = :ownerId")
    BigDecimal sumAdvanceDeductedByOwnerId(@Param("ownerId") Long ownerId);
}
