package com.example.trackbill.AIBackend.controller;
import com.example.trackbill.AIBackend.model.Farmer;
import com.example.trackbill.AIBackend.model.FarmerTransaction;
import com.example.trackbill.AIBackend.repository.FarmerTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.math.BigDecimal;
@RestController @RequestMapping("/api/farmer/reports")
public class FarmerReportsController {
 private final FarmerTransactionRepository repo;
 public FarmerReportsController(FarmerTransactionRepository r){repo=r;}
 @GetMapping("/monthly") public Map<String,Object> monthly(Authentication a,@RequestParam(required=false) LocalDate start,@RequestParam(required=false) LocalDate end){
  if(a==null||!(a.getPrincipal() instanceof Farmer f))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Farmer login required");
  LocalDate s=start==null?LocalDate.now().minusMonths(5).withDayOfMonth(1):start,e=end==null?LocalDate.now():end;
  if(s.isAfter(e))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Start date must be on or before end date");
  List<FarmerTransaction> rows=repo.findAllByFarmerIdAndTransactionDateBetweenOrderByTransactionDateDesc(f.getId(),s,e);
  Map<YearMonth,Map<String,BigDecimal>> groups=new TreeMap<>();
  for(FarmerTransaction t:rows){YearMonth ym=YearMonth.from(t.getTransactionDate());Map<String,BigDecimal> g=groups.computeIfAbsent(ym,k->{Map<String,BigDecimal> m=new LinkedHashMap<>();m.put("income",BigDecimal.ZERO);m.put("expenses",BigDecimal.ZERO);return m;});String key="INCOME".equals(t.getTransactionType())?"income":"expenses";g.put(key,g.get(key).add(t.getAmount()));}
  List<Map<String,Object>> monthly=new ArrayList<>();groups.forEach((ym,v)->{Map<String,Object> m=new LinkedHashMap<>();m.put("month",ym.toString());m.put("income",v.get("income"));m.put("expenses",v.get("expenses"));m.put("netBalance",v.get("income").subtract(v.get("expenses")));monthly.add(m);});
  BigDecimal income=rows.stream().filter(t->"INCOME".equals(t.getTransactionType())).map(FarmerTransaction::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
  BigDecimal expenses=rows.stream().filter(t->"EXPENSE".equals(t.getTransactionType())).map(FarmerTransaction::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
  return Map.of("start",s,"end",e,"income",income,"expenses",expenses,"netBalance",income.subtract(expenses),"monthly",monthly);
 }
}
