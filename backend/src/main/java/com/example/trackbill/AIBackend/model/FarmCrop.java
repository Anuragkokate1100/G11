package com.example.trackbill.AIBackend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="farmer_crop")
public class FarmCrop {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="farmer_id",nullable=false) private Farmer farmer;
 @Column(nullable=false,length=100) private String cropName;
 @Column(nullable=false,precision=10,scale=2) private BigDecimal landArea;
 @Column(nullable=false,length=30) private String areaUnit="acre";
 private LocalDate sowingDate;
 private LocalDate expectedHarvestDate;
 @Column(length=30) private String status="GROWING";
 @Column(name="created_at",updatable=false) private LocalDateTime createdAt;
 @PrePersist protected void onCreate(){createdAt=LocalDateTime.now();}
 public Long getId(){return id;} public void setId(Long v){id=v;}
 public Farmer getFarmer(){return farmer;} public void setFarmer(Farmer v){farmer=v;}
 public String getCropName(){return cropName;} public void setCropName(String v){cropName=v;}
 public BigDecimal getLandArea(){return landArea;} public void setLandArea(BigDecimal v){landArea=v;}
 public String getAreaUnit(){return areaUnit;} public void setAreaUnit(String v){areaUnit=v;}
 public LocalDate getSowingDate(){return sowingDate;} public void setSowingDate(LocalDate v){sowingDate=v;}
 public LocalDate getExpectedHarvestDate(){return expectedHarvestDate;} public void setExpectedHarvestDate(LocalDate v){expectedHarvestDate=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 public LocalDateTime getCreatedAt(){return createdAt;}
}
