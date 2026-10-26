package com.example.trackbill.AIBackend.controller;
import com.example.trackbill.AIBackend.model.*;
import com.example.trackbill.AIBackend.repository.*;
import com.example.trackbill.AIBackend.util.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@Transactional
@RestController @RequestMapping("/api/owner/service-requests")
public class OwnerServiceRequestController {
 private final TractorServiceRequestRepository requests;private final FarmerNotificationRepository notifications;
 public OwnerServiceRequestController(TractorServiceRequestRepository r,FarmerNotificationRepository n){requests=r;notifications=n;}
 @GetMapping public List<Map<String,Object>> list(){TractorOwner owner=SecurityUtil.getCurrentOwner();return requests.findAll().stream().filter(r->"PENDING".equals(r.getStatus())||(r.getTractorOwner()!=null&&r.getTractorOwner().getId().equals(owner.getId()))).map(r->dto(r,owner)).toList();}
 @PatchMapping("/{id}/status") public Map<String,Object> update(@PathVariable Long id,@RequestParam String status){
  TractorOwner owner=SecurityUtil.getCurrentOwner();TractorServiceRequest r=requests.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Service request not found"));
  String next=status==null?"":status.trim().toUpperCase();if(!Set.of("ACCEPTED","IN_PROGRESS","COMPLETED","REJECTED").contains(next))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported service request status");
  if(r.getTractorOwner()!=null&&!r.getTractorOwner().getId().equals(owner.getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Request is assigned to another tractor owner");
  if(r.getTractorOwner()==null&&!"PENDING".equals(r.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Request is no longer pending");
  if(r.getTractorOwner()==null)r.setTractorOwner(owner);r.setStatus(next);r=requests.save(r);
  FarmerNotification n=new FarmerNotification();n.setFarmer(r.getFarmer());n.setTitle("Tractor service request updated");n.setMessage("Your "+r.getServiceRequired()+" request is now "+next.replace('_',' ').toLowerCase()+".");n.setEventType("SERVICE_REQUEST");notifications.save(n);return dto(r,owner);
 }
 private Map<String,Object> dto(TractorServiceRequest r,TractorOwner viewer){Map<String,Object> m=new LinkedHashMap<>();m.put("id",r.getId());m.put("serviceRequired",r.getServiceRequired());m.put("location",r.getLocation());m.put("preferredDate",r.getPreferredDate());m.put("details",r.getDetails());m.put("status",r.getStatus());m.put("createdAt",r.getCreatedAt());m.put("farmerName",r.getFarmer().getName());m.put("farmerVillage",r.getFarmer().getVillage());if(r.getTractorOwner()!=null&&r.getTractorOwner().getId().equals(viewer.getId()))m.put("farmerPhone",r.getFarmer().getPhone());return m;}
}
