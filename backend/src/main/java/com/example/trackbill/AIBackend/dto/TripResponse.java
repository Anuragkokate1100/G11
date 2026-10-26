package com.example.trackbill.AIBackend.dto;

import com.example.trackbill.AIBackend.model.Trip;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TripResponse {
    private Long id;
    private String sugarcaneOwnerName;
    private BigDecimal weight;
    private BigDecimal dieselRequired;
    private String millName;
    private LocalDate tripDate;
    private String tractorNumber;
    private Boolean isBilled;
    private Long billingId;
    private LocalDateTime createdAt;

    public TripResponse() {
    }

    public TripResponse(Long id, String sugarcaneOwnerName, BigDecimal weight, BigDecimal dieselRequired, String millName, LocalDate tripDate, String tractorNumber, Boolean isBilled, Long billingId, LocalDateTime createdAt) {
        this.id = id;
        this.sugarcaneOwnerName = sugarcaneOwnerName;
        this.weight = weight;
        this.dieselRequired = dieselRequired;
        this.millName = millName;
        this.tripDate = tripDate;
        this.tractorNumber = tractorNumber;
        this.isBilled = isBilled;
        this.billingId = billingId;
        this.createdAt = createdAt;
    }

    public static TripResponse fromEntity(Trip trip) {
        if (trip == null) return null;
        return new TripResponse(
                trip.getId(),
                trip.getSugarcaneOwnerName(),
                trip.getWeight(),
                trip.getDieselRequired(),
                trip.getMillName(),
                trip.getTripDate(),
                trip.getTractorNumber(),
                trip.getIsBilled(),
                trip.getBillingId(),
                trip.getCreatedAt()
        );
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
