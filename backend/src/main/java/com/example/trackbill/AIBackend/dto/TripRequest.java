package com.example.trackbill.AIBackend.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class TripRequest {

    @NotBlank(message = "Sugarcane owner name is required")
    @Size(min = 2, max = 150, message = "Sugarcane owner name must be between 2 and 150 characters")
    private String sugarcaneOwnerName;

    @NotNull(message = "Sugarcane weight is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Sugarcane weight must be greater than 0")
    private BigDecimal weight;

    @NotNull(message = "Diesel required is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Diesel required cannot be negative")
    private BigDecimal dieselRequired;

    @NotBlank(message = "Sugar mill name is required")
    private String millName;

    @NotNull(message = "Trip date is required")
    private LocalDate tripDate;

    private String tractorNumber;

    public TripRequest() {
    }

    public TripRequest(String sugarcaneOwnerName, BigDecimal weight, BigDecimal dieselRequired, String millName, LocalDate tripDate, String tractorNumber) {
        this.sugarcaneOwnerName = sugarcaneOwnerName;
        this.weight = weight;
        this.dieselRequired = dieselRequired;
        this.millName = millName;
        this.tripDate = tripDate;
        this.tractorNumber = tractorNumber;
    }

    public String getSugarcaneOwnerName() {
        return sugarcaneOwnerName;
    }

    public void setSugarcaneOwnerName(String sugarcaneOwnerName) {
        this.sugarcaneOwnerName = sugarcaneOwnerName;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public BigDecimal getDieselRequired() {
        return dieselRequired;
    }

    public void setDieselRequired(BigDecimal dieselRequired) {
        this.dieselRequired = dieselRequired;
    }

    public String getMillName() {
        return millName;
    }

    public void setMillName(String millName) {
        this.millName = millName;
    }

    public LocalDate getTripDate() {
        return tripDate;
    }

    public void setTripDate(LocalDate tripDate) {
        this.tripDate = tripDate;
    }

    public String getTractorNumber() {
        return tractorNumber;
    }

    public void setTractorNumber(String tractorNumber) {
        this.tractorNumber = tractorNumber;
    }
}
