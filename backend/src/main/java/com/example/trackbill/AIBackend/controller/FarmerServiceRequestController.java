
package com.example.trackbill.AIBackend.controller;

import com.example.trackbill.AIBackend.model.*;
import com.example.trackbill.AIBackend.repository.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@Transactional
@RestController
@RequestMapping("/api/farmer/service-requests")
public class FarmerServiceRequestController {

 private final TractorServiceRequestRepository repo;
 private final FarmerNotificationRepository notifications;

 public FarmerServiceRequestController(
         TractorServiceRequestRepository repo,
         FarmerNotificationRepository notifications) {
  this.repo = repo;
  this.notifications = notifications;
 }

 // Renamed to avoid conflict with Spring's @RequestBody annotation
 public static class CreateServiceRequestRequest {

  @NotBlank
  @Size(max = 120)
  public String serviceRequired;

  @NotBlank
  @Size(max = 200)
  public String location;

  public LocalDate preferredDate;

  @Size(max = 1000)
  public String details;
 }

 private Farmer current(Authentication authentication) {
  if (authentication == null
          || !(authentication.getPrincipal() instanceof Farmer farmer)) {
   throw new ResponseStatusException(
           HttpStatus.UNAUTHORIZED,
           "Farmer login required"
   );
  }

  return farmer;
 }

 @GetMapping
 public List<Map<String, Object>> list(Authentication authentication) {
  Farmer farmer = current(authentication);

  return repo.findAllByFarmerIdOrderByCreatedAtDesc(farmer.getId())
          .stream()
          .map(this::dto)
          .toList();
 }

 @PostMapping
 public Map<String, Object> create(
         @Valid @RequestBody CreateServiceRequestRequest request,
         Authentication authentication) {

  Farmer farmer = current(authentication);

  TractorServiceRequest serviceRequest = new TractorServiceRequest();
  serviceRequest.setFarmer(farmer);
  serviceRequest.setServiceRequired(
          request.serviceRequired.trim()
  );
  serviceRequest.setLocation(request.location.trim());
  serviceRequest.setPreferredDate(request.preferredDate);
  serviceRequest.setDetails(request.details);

  serviceRequest = repo.save(serviceRequest);

  FarmerNotification notification = new FarmerNotification();
  notification.setFarmer(farmer);
  notification.setTitle("Service request submitted");
  notification.setMessage(
          "Your request for "
                  + serviceRequest.getServiceRequired()
                  + " is pending review."
  );
  notification.setEventType("SERVICE_REQUEST");

  notifications.save(notification);

  return dto(serviceRequest);
 }

 @PostMapping("/{id}/cancel")
 public Map<String, Object> cancel(
         @PathVariable Long id,
         Authentication authentication) {

  Farmer farmer = current(authentication);
  TractorServiceRequest serviceRequest = owned(id, farmer.getId());

  if (!"PENDING".equals(serviceRequest.getStatus())) {
   throw new ResponseStatusException(
           HttpStatus.CONFLICT,
           "Only pending requests can be cancelled"
   );
  }

  serviceRequest.setStatus("CANCELLED");
  serviceRequest = repo.save(serviceRequest);

  FarmerNotification notification = new FarmerNotification();
  notification.setFarmer(farmer);
  notification.setTitle("Service request cancelled");
  notification.setMessage(
          "Your "
                  + serviceRequest.getServiceRequired()
                  + " request was cancelled."
  );
  notification.setEventType("SERVICE_REQUEST");

  notifications.save(notification);

  return dto(serviceRequest);
 }

 private TractorServiceRequest owned(Long id, Long farmerId) {
  return repo.findByIdAndFarmerId(id, farmerId)
          .orElseThrow(() -> new ResponseStatusException(
                  HttpStatus.NOT_FOUND,
                  "Service request not found"
          ));
 }

 private Map<String, Object> dto(
         TractorServiceRequest serviceRequest) {

  Map<String, Object> response = new LinkedHashMap<>();

  response.put("id", serviceRequest.getId());
  response.put("serviceRequired",
          serviceRequest.getServiceRequired());
  response.put("location", serviceRequest.getLocation());
  response.put("preferredDate",
          serviceRequest.getPreferredDate());
  response.put("details", serviceRequest.getDetails());
  response.put("status", serviceRequest.getStatus());
  response.put("createdAt", serviceRequest.getCreatedAt());

  if (serviceRequest.getTractorOwner() != null) {
   response.put("ownerName",
           serviceRequest.getTractorOwner().getName());
  }

  return response;
 }
}
