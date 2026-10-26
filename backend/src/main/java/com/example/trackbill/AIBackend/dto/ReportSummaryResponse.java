package com.example.trackbill.AIBackend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReportSummaryResponse {
    private String filterType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalTrips;
    private BigDecimal totalWeight;
    private BigDecimal totalRevenue;
    private BigDecimal totalExpenses;
    private BigDecimal dieselExpenses;
    private BigDecimal labourExpenses;
    private BigDecimal otherExpenses;
    private BigDecimal netProfit;

    private List<TripResponse> trips;
    private List<ExpenseResponse> expenses;
    private List<BillResponse> bills;

    public ReportSummaryResponse() {
        this.totalTrips = 0L;
        this.totalWeight = BigDecimal.ZERO;
        this.totalRevenue = BigDecimal.ZERO;
        this.totalExpenses = BigDecimal.ZERO;
        this.dieselExpenses = BigDecimal.ZERO;
        this.labourExpenses = BigDecimal.ZERO;
        this.otherExpenses = BigDecimal.ZERO;
        this.netProfit = BigDecimal.ZERO;
    }

    // Getters and Setters
    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getTotalTrips() {
        return totalTrips;
    }

    public void setTotalTrips(Long totalTrips) {
        this.totalTrips = totalTrips;
    }

    public BigDecimal getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getDieselExpenses() {
        return dieselExpenses;
    }

    public void setDieselExpenses(BigDecimal dieselExpenses) {
        this.dieselExpenses = dieselExpenses;
    }

    public BigDecimal getLabourExpenses() {
        return labourExpenses;
    }

    public void setLabourExpenses(BigDecimal labourExpenses) {
        this.labourExpenses = labourExpenses;
    }

    public BigDecimal getOtherExpenses() {
        return otherExpenses;
    }

    public void setOtherExpenses(BigDecimal otherExpenses) {
        this.otherExpenses = otherExpenses;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
    }

    public List<TripResponse> getTrips() {
        return trips;
    }

    public void setTrips(List<TripResponse> trips) {
        this.trips = trips;
    }

    public List<ExpenseResponse> getExpenses() {
        return expenses;
    }

    public void setExpenses(List<ExpenseResponse> expenses) {
        this.expenses = expenses;
    }

    public List<BillResponse> getBills() {
        return bills;
    }

    public void setBills(List<BillResponse> bills) {
        this.bills = bills;
    }
}
