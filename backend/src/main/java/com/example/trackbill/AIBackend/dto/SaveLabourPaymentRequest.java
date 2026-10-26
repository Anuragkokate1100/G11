package com.example.trackbill.AIBackend.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class SaveLabourPaymentRequest {

    private Long billingId;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Payment items list is required")
    private List<LabourPaymentItemDto> items;

    public SaveLabourPaymentRequest() {
    }

    public SaveLabourPaymentRequest(Long billingId, LocalDate paymentDate, List<LabourPaymentItemDto> items) {
        this.billingId = billingId;
        this.paymentDate = paymentDate;
        this.items = items;
    }

    public Long getBillingId() {
        return billingId;
    }

    public void setBillingId(Long billingId) {
        this.billingId = billingId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public List<LabourPaymentItemDto> getItems() {
        return items;
    }

    public void setItems(List<LabourPaymentItemDto> items) {
        this.items = items;
    }
}
