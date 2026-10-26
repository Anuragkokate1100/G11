package com.example.trackbill.AIBackend.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="farmer_notification",indexes=@Index(name="idx_farmer_notification_owner_read",columnList="farmer_id,is_read"))
public class FarmerNotification {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="farmer_id",nullable=false) private Farmer farmer;
 @Column(nullable=false,length=100) private String title;
 @Column(nullable=false,length=500) private String message;
 @Column(name="event_type",length=40) private String eventType;
 @Column(name="is_read",nullable=false) private boolean read=false;
 @Column(name="created_at",nullable=false) private LocalDateTime createdAt;
 @PrePersist void onCreate(){createdAt=LocalDateTime.now();}
 public Long getId(){return id;} public void setId(Long v){id=v;} public Farmer getFarmer(){return farmer;} public void setFarmer(Farmer v){farmer=v;}
 public String getTitle(){return title;} public void setTitle(String v){title=v;} public String getMessage(){return message;} public void setMessage(String v){message=v;}
 public String getEventType(){return eventType;} public void setEventType(String v){eventType=v;} public boolean isRead(){return read;} public void setRead(boolean v){read=v;} public LocalDateTime getCreatedAt(){return createdAt;}
}
