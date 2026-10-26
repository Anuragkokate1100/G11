package com.example.trackbill.AIBackend.controller;
import com.example.trackbill.AIBackend.model.*;
import com.example.trackbill.AIBackend.repository.*;
import com.example.trackbill.AIBackend.util.SecurityUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/owner/bill-links") @Transactional
public class OwnerBillLinkController {
 private final BillingRepository bills;private final FarmerRepository farmers;private final FarmerBillLinkRepository links;private final FarmerNotificationRepository notifications;
 public OwnerBillLinkController(BillingRepository b,FarmerRepository f,FarmerBillLinkRepository l,FarmerNotificationRepository n){bills=b;farmers=f;links=l;notifications=n;}
 public static class LinkRequest {@NotBlank @Email public String farmerEmail;}
 @PostMapping("/{billId}") public Map<String,Object> link(@PathVariable Long billId,@Valid @RequestBody LinkRequest body){
  TractorOwner owner=SecurityUtil.getCurrentOwner();
  Billing bill=bills.findByIdAndTractorOwnerId(billId,owner.getId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Bill not found for this tractor owner"));
  Farmer farmer=farmers.findByEmail(body.farmerEmail.trim().toLowerCase()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Farmer account not found"));
  if(links.findByBillingId(billId).isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"This bill is already linked to a farmer account");
  FarmerBillLink link=new FarmerBillLink();link.setBilling(bill);link.setFarmer(farmer);link.setLinkedByOwner(owner);link=links.save(link);
  FarmerNotification n=new FarmerNotification();n.setFarmer(farmer);n.setTitle("A tractor bill was shared with you");n.setMessage("Bill #"+billId+" from "+owner.getName()+" is now available in your Bills page.");n.setEventType("BILL_LINKED");notifications.save(n);
  return Map.of("success",true,"billId",billId,"farmerEmail",farmer.getEmail(),"linkedAt",link.getCreatedAt());
 }
}
