package com.example.trackbill.AIBackend.dto;

import com.example.trackbill.AIBackend.model.LabourCategory;
import com.example.trackbill.AIBackend.model.LabourType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class LabourRequest {

    @NotBlank(message = "Labour name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Village is required")
    private String village;

    @NotNull(message = "Labour category is required (FULL_KOYTA or HALF_KOYTA)")
    private LabourCategory category;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be a valid 10-digit number")
    private String phone;

    @DecimalMin(value = "0.0", inclusive = true, message = "Advance money cannot be negative")
    private BigDecimal advanceMoney = BigDecimal.ZERO;

    @NotNull(message = "Labour type is required (LABOUR, DRIVER, HELPER)")
    private LabourType type;

    public LabourRequest() {
    }

    public LabourRequest(String name, String village, LabourCategory category, String phone, BigDecimal advanceMoney, LabourType type) {
        this.name = name;
        this.village = village;
        this.category = category;
        this.phone = phone;
        this.advanceMoney = advanceMoney != null ? advanceMoney : BigDecimal.ZERO;
        this.type = type;
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
}
