package com.example.trackbill.AIBackend.dto;

import com.example.trackbill.AIBackend.model.Billing;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class BillResponse {
    private Long id;
    private String sugarcaneOwnerName;
    private BigDecimal totalWeight;
    private BigDecimal ratePerTon;
    private BigDecimal totalAmount;
    private LocalDate billDate;
    private Integer tripCount;
    private String tripIds;
    private LocalDateTime createdAt;
    
    // Owner metadata for invoice printing
    private String ownerName;
    private String tractorNumber;
    private String ownerVillage;
    private String ownerTaluka;
    private String ownerDistrict;

    private List<TripResponse> tripDetails;

    public BillResponse() {
    }

    public static BillResponse fromEntity(Billing bill) {
        if (bill == null) return null;
        BillResponse dto = new BillResponse();
        dto.setId(bill.getId());
        dto.setSugarcaneOwnerName(bill.getSugarcaneOwnerName());
        dto.setTotalWeight(bill.getTotalWeight());
        dto.setRatePerTon(bill.getRatePerTon());
        dto.setTotalAmount(bill.getTotalAmount());
        dto.setBillDate(bill.getBillDate());
        dto.setTripCount(bill.getTripCount());
        dto.setTripIds(bill.getTripIds());
        dto.setCreatedAt(bill.getCreatedAt());

        if (bill.getTractorOwner() != null) {
            dto.setOwnerName(bill.getTractorOwner().getName());
            dto.setTractorNumber(bill.getTractorOwner().getTractorNumber());
            dto.setOwnerVillage(bill.getTractorOwner().getVillage());
            dto.setOwnerTaluka(bill.getTractorOwner().getTaluka());
            dto.setOwnerDistrict(bill.getTractorOwner().getDistrict());
        }

        return dto;
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

    public Integer getTripCount() {
        return tripCount;
    }

    public void setTripCount(Integer tripCount) {
        this.tripCount = tripCount;
    }

    public String getTripIds() {
        return tripIds;
    }

    public void setTripIds(String tripIds) {
        this.tripIds = tripIds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

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

    public String getOwnerVillage() {
        return ownerVillage;
    }

    public void setOwnerVillage(String ownerVillage) {
        this.ownerVillage = ownerVillage;
    }

    public String getOwnerTaluka() {
        return ownerTaluka;
    }

    public void setOwnerTaluka(String ownerTaluka) {
        this.ownerTaluka = ownerTaluka;
    }

    public String getOwnerDistrict() {
        return ownerDistrict;
    }

    public void setOwnerDistrict(String ownerDistrict) {
        this.ownerDistrict = ownerDistrict;
    }

    public List<TripResponse> getTripDetails() {
        return tripDetails;
    }

    public void setTripDetails(List<TripResponse> tripDetails) {
        this.tripDetails = tripDetails;
    }
}
