package com.example.trackbill.AIBackend.dto;

import jakarta.validation.constraints.*;

public class RegisterRequest {

    @NotBlank(message = "Owner name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Tractor number is required")
    @Size(min = 4, max = 50, message = "Tractor number must be valid")
    private String tractorNumber;

    @NotNull(message = "Trolly count is required")
    @Min(value = 0, message = "Trolly count must be 0 or greater")
    private Integer trollyCount;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Village is required")
    private String village;

    @NotBlank(message = "Taluka is required")
    private String taluka;

    @NotBlank(message = "District is required")
    private String district;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Pincode must be a valid 6-digit Indian postal code")
    private String pincode;

    public RegisterRequest() {
    }

    public RegisterRequest(String name, String tractorNumber, Integer trollyCount, String email, String password, String village, String taluka, String district, String pincode) {
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
        this.tractorNumber = tractorNumber != null ? tractorNumber.trim().toUpperCase() : null;
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
        this.email = email != null ? email.trim().toLowerCase() : null;
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
}
