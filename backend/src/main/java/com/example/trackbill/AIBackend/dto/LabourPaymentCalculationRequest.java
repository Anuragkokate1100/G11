package com.example.trackbill.AIBackend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class LabourPaymentCalculationRequest {

    @NotNull(message = "Total sugarcane weight is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Total weight must be greater than 0")
    private BigDecimal totalWeight;

    @NotNull(message = "Rate per ton is required")
    @DecimalMin(value = "1.0", inclusive = true, message = "Rate per ton must be at least ₹1.00")
    private BigDecimal ratePerTon;

    private List<Long> labourIds;

    private LocalDate paymentDate;

    private Long billingId;

    public LabourPaymentCalculationRequest() {
    }

    public LabourPaymentCalculationRequest(BigDecimal totalWeight, BigDecimal ratePerTon, List<Long> labourIds, LocalDate paymentDate, Long billingId) {
        this.totalWeight = totalWeight;
        this.ratePerTon = ratePerTon;
        this.labourIds = labourIds;
        this.paymentDate = paymentDate;
        this.billingId = billingId;
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

    public List<Long> getLabourIds() {
        return labourIds;
    }

    public void setLabourIds(List<Long> labourIds) {
        this.labourIds = labourIds;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Long getBillingId() {
        return billingId;
    }

    public void setBillingId(Long billingId) {
        this.billingId = billingId;
    }
}
