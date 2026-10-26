package com.example.trackbill.AIBackend.dto;

import java.math.BigDecimal;
import java.util.List;

public class LabourPaymentCalculationResponse {
    private BigDecimal totalWeight;
    private BigDecimal ratePerTon;
    private int totalWorkers;
    private int fullKoytaCount;
    private int halfKoytaCount;
    private BigDecimal baseSharePerWorker;
    private BigDecimal fullKoytaShareWeight;
    private BigDecimal halfKoytaShareWeight;
    private BigDecimal totalCalculatedGross;
    private BigDecimal totalAdvanceDeducted;
    private BigDecimal totalNetPayable;
    private List<LabourPaymentItemDto> items;

    public LabourPaymentCalculationResponse() {
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

    public int getTotalWorkers() {
        return totalWorkers;
    }

    public void setTotalWorkers(int totalWorkers) {
        this.totalWorkers = totalWorkers;
    }

    public int getFullKoytaCount() {
        return fullKoytaCount;
    }

    public void setFullKoytaCount(int fullKoytaCount) {
        this.fullKoytaCount = fullKoytaCount;
    }

    public int getHalfKoytaCount() {
        return halfKoytaCount;
    }

    public void setHalfKoytaCount(int halfKoytaCount) {
        this.halfKoytaCount = halfKoytaCount;
    }

    public BigDecimal getBaseSharePerWorker() {
        return baseSharePerWorker;
    }

    public void setBaseSharePerWorker(BigDecimal baseSharePerWorker) {
        this.baseSharePerWorker = baseSharePerWorker;
    }

    public BigDecimal getFullKoytaShareWeight() {
        return fullKoytaShareWeight;
    }

    public void setFullKoytaShareWeight(BigDecimal fullKoytaShareWeight) {
        this.fullKoytaShareWeight = fullKoytaShareWeight;
    }

    public BigDecimal getHalfKoytaShareWeight() {
        return halfKoytaShareWeight;
    }

    public void setHalfKoytaShareWeight(BigDecimal halfKoytaShareWeight) {
        this.halfKoytaShareWeight = halfKoytaShareWeight;
    }

    public BigDecimal getTotalCalculatedGross() {
        return totalCalculatedGross;
    }

    public void setTotalCalculatedGross(BigDecimal totalCalculatedGross) {
        this.totalCalculatedGross = totalCalculatedGross;
    }

    public BigDecimal getTotalAdvanceDeducted() {
        return totalAdvanceDeducted;
    }

    public void setTotalAdvanceDeducted(BigDecimal totalAdvanceDeducted) {
        this.totalAdvanceDeducted = totalAdvanceDeducted;
    }

    public BigDecimal getTotalNetPayable() {
        return totalNetPayable;
    }

    public void setTotalNetPayable(BigDecimal totalNetPayable) {
        this.totalNetPayable = totalNetPayable;
    }

    public List<LabourPaymentItemDto> getItems() {
        return items;
    }

    public void setItems(List<LabourPaymentItemDto> items) {
        this.items = items;
    }
}
