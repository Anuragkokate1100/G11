package com.example.trackbill.AIBackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing")
public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tractor_owner_id", nullable = false)
    @JsonIgnore
    private TractorOwner tractorOwner;

    @Column(name = "sugarcane_owner_name", length = 150)
    private String sugarcaneOwnerName;

    @Column(name = "total_weight", nullable = false, precision = 12, scale = 3)
    private BigDecimal totalWeight;

    @Column(name = "rate_per_ton", nullable = false, precision = 10, scale = 2)
    private BigDecimal ratePerTon;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "bill_date", nullable = false)
    private LocalDate billDate;

    @Column(name = "trip_ids", length = 500)
    private String tripIds;

    @Column(name = "trip_count")
    private Integer tripCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Billing() {
    }

    public Billing(Long id, TractorOwner tractorOwner, String sugarcaneOwnerName, BigDecimal totalWeight, BigDecimal ratePerTon, BigDecimal totalAmount, LocalDate billDate, String tripIds, Integer tripCount) {
        this.id = id;
        this.tractorOwner = tractorOwner;
        this.sugarcaneOwnerName = sugarcaneOwnerName;
        this.totalWeight = totalWeight;
        this.ratePerTon = ratePerTon;
        this.totalAmount = totalAmount;
        this.billDate = billDate;
        this.tripIds = tripIds;
        this.tripCount = tripCount;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TractorOwner getTractorOwner() {
        return tractorOwner;
    }

    public void setTractorOwner(TractorOwner tractorOwner) {
        this.tractorOwner = tractorOwner;
    }

    public String getSugarcaneOwnerName() {
        return sugarcaneOwnerName;
    }

    public void setSugarcaneOwnerName(String sugarcaneOwnerName) {
        this.sugarcaneOwnerName = sugarcaneOwnerName;
    }

    public BigDecimal getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }

    public BigDecimal getRatePerTon() {
        return ratePerTon;
    }

    public void setRatePerTon(BigDecimal ratePerTon) {
        this.ratePerTon = ratePerTon;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }

    public String getTripIds() {
        return tripIds;
    }

    public void setTripIds(String tripIds) {
        this.tripIds = tripIds;
    }

    public Integer getTripCount() {
        return tripCount;
    }

    public void setTripCount(Integer tripCount) {
        this.tripCount = tripCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
