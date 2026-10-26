package com.example.trackbill.AIBackend.controller;
import com.example.trackbill.AIBackend.model.Farmer;
import com.example.trackbill.AIBackend.model.FarmerNotification;
import com.example.trackbill.AIBackend.repository.FarmerNotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/farmer/notifications")
public class FarmerNotificationController {
 private final FarmerNotificationRepository repo;
 public FarmerNotificationController(FarmerNotificationRepository r){repo=r;}
 private Farmer current(Authentication a){if(a==null||!(a.getPrincipal() instanceof Farmer f))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Farmer login required");return f;}
 @GetMapping public Map<String,Object> list(Authentication a){Farmer f=current(a);List<Map<String,Object>> items=repo.findAllByFarmerIdOrderByCreatedAtDesc(f.getId()).stream().map(this::dto).toList();return Map.of("items",items,"unreadCount",repo.countByFarmerIdAndReadFalse(f.getId()));}
 @PatchMapping("/{id}/read") public Map<String,Object> markRead(@PathVariable Long id,Authentication a){FarmerNotification n=owned(id,current(a).getId());n.setRead(true);repo.save(n);return dto(n);}
 @PostMapping("/read-all") public Map<String,Object> markAllRead(Authentication a){List<FarmerNotification> ns=repo.findAllByFarmerIdAndReadFalse(current(a).getId());ns.forEach(n->n.setRead(true));repo.saveAll(ns);return Map.of("updated",ns.size());}
 private FarmerNotification owned(Long id,Long fid){return repo.findByIdAndFarmerId(id,fid).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Notification not found"));}
 private Map<String,Object> dto(FarmerNotification n){Map<String,Object> m=new LinkedHashMap<>();m.put("id",n.getId());m.put("title",n.getTitle());m.put("message",n.getMessage());m.put("eventType",n.getEventType());m.put("read",n.isRead());m.put("createdAt",n.getCreatedAt());return m;}
}
