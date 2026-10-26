package com.example.trackbill.AIBackend.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="farmer_bill_link",uniqueConstraints=@UniqueConstraint(name="uk_farmer_bill_link_billing",columnNames="billing_id"))
public class FarmerBillLink {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="farmer_id",nullable=false) private Farmer farmer;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="billing_id",nullable=false) private Billing billing;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="linked_by_owner_id",nullable=false) private TractorOwner linkedByOwner;
 @Column(name="created_at",nullable=false) private LocalDateTime createdAt;
 @PrePersist void onCreate(){createdAt=LocalDateTime.now();}
 public Long getId(){return id;} public Farmer getFarmer(){return farmer;} public void setFarmer(Farmer v){farmer=v;}
 public Billing getBilling(){return billing;} public void setBilling(Billing v){billing=v;}
 public TractorOwner getLinkedByOwner(){return linkedByOwner;} public void setLinkedByOwner(TractorOwner v){linkedByOwner=v;}
 public LocalDateTime getCreatedAt(){return createdAt;}
}
