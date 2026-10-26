package com.example.trackbill.AIBackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "farmer", uniqueConstraints = {@UniqueConstraint(columnNames = "email"), @UniqueConstraint(columnNames = "phone")})
public class Farmer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, length=100) private String name;
    @Column(nullable=false, unique=true, length=100) private String email;
    @Column(nullable=false, unique=true, length=20) private String phone;
    @JsonIgnore @Column(nullable=false, length=255) private String password;
    @Column(nullable=false, length=100) private String village;
    @Column(nullable=false, length=100) private String taluka;
    @Column(nullable=false, length=100) private String district;
    @Column(nullable=false, length=100) private String state = "Maharashtra";
    @Column(length=10) private String pincode;
    @Column(name="created_at", updatable=false) private LocalDateTime createdAt;
    @Column(name="updated_at") private LocalDateTime updatedAt;
    @PrePersist protected void onCreate(){createdAt=LocalDateTime.now();updatedAt=createdAt;}
    @PreUpdate protected void onUpdate(){updatedAt=LocalDateTime.now();}
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public String getPassword(){return password;} public void setPassword(String v){password=v;}
    public String getVillage(){return village;} public void setVillage(String v){village=v;}
    public String getTaluka(){return taluka;} public void setTaluka(String v){taluka=v;}
    public String getDistrict(){return district;} public void setDistrict(String v){district=v;}
    public String getState(){return state;} public void setState(String v){state=v;}
    public String getPincode(){return pincode;} public void setPincode(String v){pincode=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public LocalDateTime getUpdatedAt(){return updatedAt;}
}
