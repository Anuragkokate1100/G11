package com.example.trackbill.AIBackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tractor_owner", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
public class TractorOwner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "tractor_number", nullable = false, length = 50)
    private String tractorNumber;

    @Column(name = "trolly_count", nullable = false)
    private Integer trollyCount = 1;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String village;

    @Column(nullable = false, length = 100)
    private String taluka;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(nullable = false, length = 10)
    private String pincode;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TractorOwner() {
    }

    public TractorOwner(Long id, String name, String tractorNumber, Integer trollyCount, String email, String password, String village, String taluka, String district, String pincode) {
        this.id = id;
        this.name = name;
        this.tractorNumber = tractorNumber;
        this.trollyCount = trollyCount;
        this.email = email;
        this.password = password;
        this.village = village;
        this.taluka = taluka;
        this.district = district;
        this.pincode = pincode;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getTaluka() {
        return taluka;
    }

    public void setTaluka(String taluka) {
        this.taluka = taluka;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
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
