package com.example.trackbill.AIBackend.dto;

import com.example.trackbill.AIBackend.model.Labour;
import com.example.trackbill.AIBackend.model.LabourCategory;
import com.example.trackbill.AIBackend.model.LabourType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class LabourResponse {
    private Long id;
    private String name;
    private String village;
    private LabourCategory category;
    private String phone;
    private BigDecimal advanceMoney;
    private LabourType type;
    private LocalDateTime createdAt;

    public LabourResponse() {
    }

    public LabourResponse(Long id, String name, String village, LabourCategory category, String phone, BigDecimal advanceMoney, LabourType type, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.village = village;
        this.category = category;
        this.phone = phone;
        this.advanceMoney = advanceMoney;
        this.type = type;
        this.createdAt = createdAt;
    }

    public static LabourResponse fromEntity(Labour labour) {
        if (labour == null) return null;
        return new LabourResponse(
                labour.getId(),
                labour.getName(),
                labour.getVillage(),
                labour.getCategory(),
                labour.getPhone(),
                labour.getAdvanceMoney(),
                labour.getType(),
                labour.getCreatedAt()
        );
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BigDecimal getAdvanceMoney() {
        return advanceMoney;
    }

    public void setAdvanceMoney(BigDecimal advanceMoney) {
        this.advanceMoney = advanceMoney;
    }

    public LabourType getType() {
        return type;
    }

    public void setType(LabourType type) {
        this.type = type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
