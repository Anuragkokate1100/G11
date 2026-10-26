package com.example.trackbill.AIBackend.dto;
import com.example.trackbill.AIBackend.model.FarmCrop; import java.math.BigDecimal; import java.time.LocalDate;
public class FarmCropResponse {
 private Long id; private String cropName,areaUnit,status; private BigDecimal landArea; private LocalDate sowingDate,expectedHarvestDate;
 public FarmCropResponse(FarmCrop c){id=c.getId();cropName=c.getCropName();landArea=c.getLandArea();areaUnit=c.getAreaUnit();status=c.getStatus();sowingDate=c.getSowingDate();expectedHarvestDate=c.getExpectedHarvestDate();}
 public Long getId(){return id;} public String getCropName(){return cropName;} public BigDecimal getLandArea(){return landArea;} public String getAreaUnit(){return areaUnit;} public String getStatus(){return status;} public LocalDate getSowingDate(){return sowingDate;} public LocalDate getExpectedHarvestDate(){return expectedHarvestDate;}
}
