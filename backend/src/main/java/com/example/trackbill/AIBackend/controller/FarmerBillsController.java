package com.example.trackbill.AIBackend.controller;
import com.example.trackbill.AIBackend.model.*;
import com.example.trackbill.AIBackend.repository.FarmerBillLinkRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/farmer/bills") @Transactional
public class FarmerBillsController {
 private final FarmerBillLinkRepository links;
 public FarmerBillsController(FarmerBillLinkRepository links){this.links=links;}
 @GetMapping public List<Map<String,Object>> list(Authentication a){if(a==null||!(a.getPrincipal() instanceof Farmer f))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Farmer login required");return links.findAllByFarmerIdOrderByCreatedAtDesc(f.getId()).stream().map(this::dto).toList();}
 private Map<String,Object> dto(FarmerBillLink l){Billing b=l.getBilling();Map<String,Object> m=new LinkedHashMap<>();m.put("billNumber",b.getId());m.put("ownerName",b.getTractorOwner().getName());m.put("tractorNumber",b.getTractorOwner().getTractorNumber());m.put("serviceDetails","Tractor transport / "+(b.getTripCount()==null?0:b.getTripCount())+" trip(s), "+(b.getTotalWeight()==null?"—":b.getTotalWeight())+" tons");m.put("billDate",b.getBillDate());m.put("amount",b.getTotalAmount());m.put("paymentStatus","Not tracked in the existing billing schema");m.put("linkedAt",l.getCreatedAt());return m;}
}
