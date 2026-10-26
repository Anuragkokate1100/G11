package com.example.trackbill.AIBackend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BillRequest {

    private List<Long> selectedTripIds;

    private BigDecimal manualWeight;

    @NotNull(message = "Rate per ton is required")
    @DecimalMin(value = "1.0", inclusive = true, message = "Rate per ton must be at least ₹1.00")
    private BigDecimal ratePerTon;

    @NotNull(message = "Bill date is required")
    private LocalDate billDate;

    private String sugarcaneOwnerName;

    public BillRequest() {
    }

    public BillRequest(List<Long> selectedTripIds, BigDecimal manualWeight, BigDecimal ratePerTon, LocalDate billDate, String sugarcaneOwnerName) {
        this.selectedTripIds = selectedTripIds;
        this.manualWeight = manualWeight;
        this.ratePerTon = ratePerTon;
        this.billDate = billDate;
        this.sugarcaneOwnerName = sugarcaneOwnerName;
    }

    public List<Long> getSelectedTripIds() {
        return selectedTripIds;
    }

    public void setSelectedTripIds(List<Long> selectedTripIds) {
        this.selectedTripIds = selectedTripIds;
    }

    public BigDecimal getManualWeight() {
        return manualWeight;
    }

    public void setManualWeight(BigDecimal manualWeight) {
        this.manualWeight = manualWeight;
    }

    public BigDecimal getRatePerTon() {
        return ratePerTon;
    }

    public void setRatePerTon(BigDecimal ratePerTon) {
        this.ratePerTon = ratePerTon;
    }

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }

    public String getSugarcaneOwnerName() {
        return sugarcaneOwnerName;
    }

    public void setSugarcaneOwnerName(String sugarcaneOwnerName) {
        this.sugarcaneOwnerName = sugarcaneOwnerName;
    }
}
