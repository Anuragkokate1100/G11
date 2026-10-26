package com.example.trackbill.AIBackend.dto;
import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.LocalDate;
public class FarmCropRequest {
 @NotBlank @Size(max=100) private String cropName;
 @NotNull @DecimalMin(value="0.01") @Digits(integer=8,fraction=2) private BigDecimal landArea;
 @NotBlank @Pattern(regexp="acre|hectare|guntha") private String areaUnit="acre";
 private LocalDate sowingDate; private LocalDate expectedHarvestDate;
 @Pattern(regexp="PLANNED|GROWING|HARVESTED") private String status="GROWING";
 public String getCropName(){return cropName;} public void setCropName(String v){cropName=v;}
 public BigDecimal getLandArea(){return landArea;} public void setLandArea(BigDecimal v){landArea=v;}
 public String getAreaUnit(){return areaUnit;} public void setAreaUnit(String v){areaUnit=v;}
 public LocalDate getSowingDate(){return sowingDate;} public void setSowingDate(LocalDate v){sowingDate=v;}
 public LocalDate getExpectedHarvestDate(){return expectedHarvestDate;} public void setExpectedHarvestDate(LocalDate v){expectedHarvestDate=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
}
