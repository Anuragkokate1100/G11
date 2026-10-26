package com.example.trackbill.AIBackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "labour_payment")
public class LabourPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "labour_id", nullable = false)
    private Long labourId;

    @Column(name = "labour_name", nullable = false, length = 100)
    private String labourName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 30)
    private LabourCategory category;

    @Column(name = "billing_id")
    private Long billingId;

    @Column(name = "assigned_weight", nullable = false, precision = 10, scale = 3)
    private BigDecimal assignedWeight;

    @Column(name = "rate_per_ton", nullable = false, precision = 10, scale = 2)
    private BigDecimal ratePerTon;

    @Column(name = "calculated_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal calculatedAmount;

    @Column(name = "advance_deducted", nullable = false, precision = 12, scale = 2)
    private BigDecimal advanceDeducted = BigDecimal.ZERO;

    @Column(name = "final_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalAmount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tractor_owner_id", nullable = false)
    @JsonIgnore
    private TractorOwner tractorOwner;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public LabourPayment() {
    }

    public LabourPayment(Long id, Long labourId, String labourName, LabourCategory category, Long billingId, BigDecimal assignedWeight, BigDecimal ratePerTon, BigDecimal calculatedAmount, BigDecimal advanceDeducted, BigDecimal finalAmount, LocalDate paymentDate, TractorOwner tractorOwner) {
        this.id = id;
        this.labourId = labourId;
        this.labourName = labourName;
        this.category = category;
        this.billingId = billingId;
        this.assignedWeight = assignedWeight;
        this.ratePerTon = ratePerTon;
        this.calculatedAmount = calculatedAmount;
        this.advanceDeducted = advanceDeducted != null ? advanceDeducted : BigDecimal.ZERO;
        this.finalAmount = finalAmount;
        this.paymentDate = paymentDate;
        this.tractorOwner = tractorOwner;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.advanceDeducted == null) {
            this.advanceDeducted = BigDecimal.ZERO;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLabourId() {
        return labourId;
    }

    public void setLabourId(Long labourId) {
        this.labourId = labourId;
    }

    public String getLabourName() {
        return labourName;
    }

    public void setLabourName(String labourName) {
        this.labourName = labourName;
    }

    public LabourCategory getCategory() {
        return category;
    }

    public void setCategory(LabourCategory category) {
        this.category = category;
    }

    public Long getBillingId() {
        return billingId;
    }

    public void setBillingId(Long billingId) {
        this.billingId = billingId;
    }

    public BigDecimal getAssignedWeight() {
        return assignedWeight;
    }

    public void setAssignedWeight(BigDecimal assignedWeight) {
        this.assignedWeight = assignedWeight;
    }

    public BigDecimal getRatePerTon() {
        return ratePerTon;
    }

    public void setRatePerTon(BigDecimal ratePerTon) {
        this.ratePerTon = ratePerTon;
    }

    public BigDecimal getCalculatedAmount() {
        return calculatedAmount;
    }

    public void setCalculatedAmount(BigDecimal calculatedAmount) {
        this.calculatedAmount = calculatedAmount;
    }

    public BigDecimal getAdvanceDeducted() {
        return advanceDeducted;
    }

    public void setAdvanceDeducted(BigDecimal advanceDeducted) {
        this.advanceDeducted = advanceDeducted;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public TractorOwner getTractorOwner() {
        return tractorOwner;
    }

    public void setTractorOwner(TractorOwner tractorOwner) {
        this.tractorOwner = tractorOwner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
