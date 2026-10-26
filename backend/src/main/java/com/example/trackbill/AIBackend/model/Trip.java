package com.example.trackbill.AIBackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip")
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sugarcane_owner_name", nullable = false, length = 150)
    private String sugarcaneOwnerName;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal weight;

    @Column(name = "diesel_required", nullable = false, precision = 10, scale = 2)
    private BigDecimal dieselRequired = BigDecimal.ZERO;

    @Column(name = "mill_name", nullable = false, length = 150)
    private String millName;

    @Column(name = "trip_date", nullable = false)
    private LocalDate tripDate;

    @Column(name = "tractor_number", nullable = false, length = 50)
    private String tractorNumber;

    @Column(name = "is_billed", nullable = false)
    private Boolean isBilled = false;

    @Column(name = "billing_id")
    private Long billingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tractor_owner_id", nullable = false)
    @JsonIgnore
    private TractorOwner tractorOwner;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Trip() {
    }

    public Trip(Long id, String sugarcaneOwnerName, BigDecimal weight, BigDecimal dieselRequired, String millName, LocalDate tripDate, String tractorNumber, TractorOwner tractorOwner) {
        this.id = id;
        this.sugarcaneOwnerName = sugarcaneOwnerName;
        this.weight = weight;
        this.dieselRequired = dieselRequired != null ? dieselRequired : BigDecimal.ZERO;
        this.millName = millName;
        this.tripDate = tripDate;
        this.tractorNumber = tractorNumber;
        this.tractorOwner = tractorOwner;
        this.isBilled = false;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isBilled == null) {
            this.isBilled = false;
        }
        if (this.dieselRequired == null) {
            this.dieselRequired = BigDecimal.ZERO;
        }
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

    public String getSugarcaneOwnerName() {
        return sugarcaneOwnerName;
    }

    public void setSugarcaneOwnerName(String sugarcaneOwnerName) {
        this.sugarcaneOwnerName = sugarcaneOwnerName;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getDieselRequired() {
        return dieselRequired;
    }

    public void setDieselRequired(BigDecimal dieselRequired) {
        this.dieselRequired = dieselRequired;
    }

    public String getMillName() {
        return millName;
    }

    public void setMillName(String millName) {
        this.millName = millName;
    }

    public LocalDate getTripDate() {
        return tripDate;
    }

    public void setTripDate(LocalDate tripDate) {
        this.tripDate = tripDate;
    }

    public String getTractorNumber() {
        return tractorNumber;
    }

    public void setTractorNumber(String tractorNumber) {
        this.tractorNumber = tractorNumber;
    }

    public Boolean getIsBilled() {
        return isBilled;
    }

    public void setIsBilled(Boolean isBilled) {
        this.isBilled = isBilled;
    }

    public Long getBillingId() {
        return billingId;
    }

    public void setBillingId(Long billingId) {
        this.billingId = billingId;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
