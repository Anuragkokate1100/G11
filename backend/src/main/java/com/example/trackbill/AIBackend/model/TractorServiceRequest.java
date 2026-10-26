package com.example.trackbill.AIBackend.model;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity @Table(name="farmer_service_request",indexes=@Index(name="idx_service_request_farmer_status",columnList="farmer_id,status"))
public class TractorServiceRequest {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="farmer_id",nullable=false) private Farmer farmer;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="tractor_owner_id") private TractorOwner tractorOwner;
 @Column(name="service_required",nullable=false,length=120) private String serviceRequired;
 @Column(nullable=false,length=200) private String location;
 @Column(name="preferred_date") private LocalDate preferredDate;
 @Column(length=1000) private String details;
 @Column(nullable=false,length=20) private String status="PENDING";
 @Column(name="created_at",nullable=false,updatable=false) private LocalDateTime createdAt;
 @PrePersist void onCreate(){createdAt=LocalDateTime.now();}
 public Long getId(){return id;} public Farmer getFarmer(){return farmer;} public void setFarmer(Farmer v){farmer=v;}
 public TractorOwner getTractorOwner(){return tractorOwner;} public void setTractorOwner(TractorOwner v){tractorOwner=v;}
 public String getServiceRequired(){return serviceRequired;} public void setServiceRequired(String v){serviceRequired=v;}
 public String getLocation(){return location;} public void setLocation(String v){location=v;}
 public LocalDate getPreferredDate(){return preferredDate;} public void setPreferredDate(LocalDate v){preferredDate=v;}
 public String getDetails(){return details;} public void setDetails(String v){details=v;}
 public String getStatus(){return status;} public void setStatus(String v){status=v;}
 public LocalDateTime getCreatedAt(){return createdAt;}
}
