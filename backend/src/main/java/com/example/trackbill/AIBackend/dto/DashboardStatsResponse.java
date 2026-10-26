package com.example.trackbill.AIBackend.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardStatsResponse {
    private String ownerName;
    private String tractorNumber;
    private Integer trollyCount;
    private Long totalTrips;
    private BigDecimal totalWeight;
    private BigDecimal totalDiesel;
    private BigDecimal totalExpenses;
    private BigDecimal totalRevenue;
    private BigDecimal netProfit;
    private Long totalLabour;
    private BigDecimal totalLabourAdvance;
    private List<TripResponse> recentTrips;
    private List<ExpenseResponse> recentExpenses;

    public DashboardStatsResponse() {
        this.totalTrips = 0L;
        this.totalWeight = BigDecimal.ZERO;
        this.totalDiesel = BigDecimal.ZERO;
        this.totalExpenses = BigDecimal.ZERO;
        this.totalRevenue = BigDecimal.ZERO;
        this.netProfit = BigDecimal.ZERO;
        this.totalLabour = 0L;
        this.totalLabourAdvance = BigDecimal.ZERO;
    }

    // Getters and Setters
    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getTractorNumber() {
        return tractorNumber;
    }

    public void setTractorNumber(String tractorNumber) {
        this.tractorNumber = tractorNumber;
    }

    public Integer getTrollyCount() {
        return trollyCount;
    }

    public void setTrollyCount(Integer trollyCount) {
        this.trollyCount = trollyCount;
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

    public BigDecimal getTotalDiesel() {
        return totalDiesel;
    }

    public void setTotalDiesel(BigDecimal totalDiesel) {
        this.totalDiesel = totalDiesel;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(BigDecimal totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }

    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
    }

    public Long getTotalLabour() {
        return totalLabour;
    }

    public void setTotalLabour(Long totalLabour) {
        this.totalLabour = totalLabour;
    }

    public BigDecimal getTotalLabourAdvance() {
        return totalLabourAdvance;
    }

    public void setTotalLabourAdvance(BigDecimal totalLabourAdvance) {
        this.totalLabourAdvance = totalLabourAdvance;
    }

    public List<TripResponse> getRecentTrips() {
        return recentTrips;
    }

    public void setRecentTrips(List<TripResponse> recentTrips) {
        this.recentTrips = recentTrips;
    }

    public List<ExpenseResponse> getRecentExpenses() {
        return recentExpenses;
    }

    public void setRecentExpenses(List<ExpenseResponse> recentExpenses) {
        this.recentExpenses = recentExpenses;
    }
}
