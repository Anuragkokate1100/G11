package com.example.trackbill.AIBackend.dto;

import com.example.trackbill.AIBackend.model.TractorOwner;

public class OwnerProfileDto {
    private Long id;
    private String name;
    private String tractorNumber;
    private Integer trollyCount;
    private String email;
    private String village;
    private String taluka;
    private String district;
    private String pincode;

    public OwnerProfileDto() {
    }

    public OwnerProfileDto(Long id, String name, String tractorNumber, Integer trollyCount, String email, String village, String taluka, String district, String pincode) {
        this.id = id;
        this.name = name;
        this.tractorNumber = tractorNumber;
        this.trollyCount = trollyCount;
        this.email = email;
        this.village = village;
        this.taluka = taluka;
        this.district = district;
        this.pincode = pincode;
    }

    public static OwnerProfileDto fromEntity(TractorOwner owner) {
        if (owner == null) return null;
        return new OwnerProfileDto(
                owner.getId(),
                owner.getName(),
                owner.getTractorNumber(),
                owner.getTrollyCount(),
                owner.getEmail(),
                owner.getVillage(),
                owner.getTaluka(),
                owner.getDistrict(),
                owner.getPincode()
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
}
