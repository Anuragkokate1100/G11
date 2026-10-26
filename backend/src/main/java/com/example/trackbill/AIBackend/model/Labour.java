package com.example.trackbill.AIBackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "labour")
public class Labour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String village;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LabourCategory category = LabourCategory.FULL_KOYTA;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "advance_money", nullable = false, precision = 12, scale = 2)
    private BigDecimal advanceMoney = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LabourType type = LabourType.LABOUR;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tractor_owner_id", nullable = false)
    @JsonIgnore
    private TractorOwner tractorOwner;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Labour() {
    }

    public Labour(Long id, String name, String village, LabourCategory category, String phone, BigDecimal advanceMoney, LabourType type, TractorOwner tractorOwner) {
        this.id = id;
        this.name = name;
        this.village = village;
        this.category = category;
        this.phone = phone;
        this.advanceMoney = advanceMoney != null ? advanceMoney : BigDecimal.ZERO;
        this.type = type;
        this.tractorOwner = tractorOwner;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.advanceMoney == null) {
            this.advanceMoney = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public TractorOwner getTractorOwner() {
        return tractorOwner;
    }

    public void setTractorOwner(TractorOwner tractorOwner) {
        this.tractorOwner = tractorOwner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
