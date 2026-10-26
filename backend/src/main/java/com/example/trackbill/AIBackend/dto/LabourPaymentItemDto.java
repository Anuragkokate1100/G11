package com.example.trackbill.AIBackend.dto;

import com.example.trackbill.AIBackend.model.LabourCategory;
import com.example.trackbill.AIBackend.model.LabourType;
import java.math.BigDecimal;

public class LabourPaymentItemDto {
    private Long labourId;
    private String labourName;
    private String village;
    private LabourCategory category;
    private LabourType type;
    private BigDecimal assignedWeight;
    private BigDecimal ratePerTon;
    private BigDecimal grossAmount;
    private BigDecimal currentAdvance;
    private BigDecimal advanceDeducted;
    private BigDecimal finalAmount;

    public LabourPaymentItemDto() {
    }

    public LabourPaymentItemDto(Long labourId, String labourName, String village, LabourCategory category, LabourType type, BigDecimal assignedWeight, BigDecimal ratePerTon, BigDecimal grossAmount, BigDecimal currentAdvance, BigDecimal advanceDeducted, BigDecimal finalAmount) {
        this.labourId = labourId;
        this.labourName = labourName;
        this.village = village;
        this.category = category;
        this.type = type;
        this.assignedWeight = assignedWeight;
        this.ratePerTon = ratePerTon;
        this.grossAmount = grossAmount;
        this.currentAdvance = currentAdvance;
        this.advanceDeducted = advanceDeducted;
        this.finalAmount = finalAmount;
    }

    // Getters and Setters
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

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public LabourCategory getCategory() {
        return category;
    }

    public void setCategory(LabourCategory category) {
        this.category = category;
    }

    public LabourType getType() {
        return type;
    }

    public void setType(LabourType type) {
        this.type = type;
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

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getCurrentAdvance() {
        return currentAdvance;
    }

    public void setCurrentAdvance(BigDecimal currentAdvance) {
        this.currentAdvance = currentAdvance;
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
}
