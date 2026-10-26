package com.example.trackbill.AIBackend.controller;

import com.example.trackbill.AIBackend.dto.FarmerTransactionRequest;
import com.example.trackbill.AIBackend.model.Farmer;
import com.example.trackbill.AIBackend.model.FarmerTransaction;
import com.example.trackbill.AIBackend.repository.FarmerTransactionRepository;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/farmer/transactions")
public class FarmerTransactionController {

 private final FarmerTransactionRepository repo;

 public FarmerTransactionController(FarmerTransactionRepository repo) {
  this.repo = repo;
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
 public List<Map<String, Object>> list(
         Authentication authentication,
         @RequestParam(required = false) String type,
         @RequestParam(required = false) LocalDate start,
         @RequestParam(required = false) LocalDate end) {

  Farmer farmer = current(authentication);
  List<FarmerTransaction> transactions;

  if (start != null && end != null) {
   transactions =
           repo.findAllByFarmer_IdAndTransactionDateBetweenOrderByTransactionDateDesc(
                   farmer.getId(), start, end
           );
  } else {
   transactions =
           repo.findAllByFarmer_IdOrderByTransactionDateDescIdDesc(
                   farmer.getId()
           );
  }

  return transactions.stream()
          .filter(transaction ->
                  type == null
                          || type.isBlank()
                          || transaction.getTransactionType()
                          .equalsIgnoreCase(type))
          .map(this::dto)
          .toList();
 }

 @PostMapping
 public Map<String, Object> create(
         @Valid @RequestBody FarmerTransactionRequest request,
         Authentication authentication) {

  FarmerTransaction transaction = new FarmerTransaction();
  transaction.setFarmer(current(authentication));

  apply(transaction, request);

  return dto(repo.save(transaction));
 }

 @PutMapping("/{id}")
 public Map<String, Object> update(
         @PathVariable Long id,
         @Valid @RequestBody FarmerTransactionRequest request,
         Authentication authentication) {

  Farmer farmer = current(authentication);
  FarmerTransaction transaction = owned(id, farmer.getId());

  apply(transaction, request);

  return dto(repo.save(transaction));
 }

 @DeleteMapping("/{id}")
 @ResponseStatus(HttpStatus.NO_CONTENT)
 public void delete(
         @PathVariable Long id,
         Authentication authentication) {

  Farmer farmer = current(authentication);
  repo.delete(owned(id, farmer.getId()));
 }

 @GetMapping("/summary")
 public Map<String, Object> summary(
         Authentication authentication,
         @RequestParam(required = false) LocalDate start,
         @RequestParam(required = false) LocalDate end) {

  LocalDate startDate = start == null
          ? LocalDate.now().withDayOfMonth(1)
          : start;

  LocalDate endDate = end == null
          ? LocalDate.now()
          : end;

  if (startDate.isAfter(endDate)) {
   throw new ResponseStatusException(
           HttpStatus.BAD_REQUEST,
           "Start date must be on or before end date"
   );
  }

  Farmer farmer = current(authentication);

  List<FarmerTransaction> transactions =
          repo.findAllByFarmer_IdAndTransactionDateBetweenOrderByTransactionDateDesc(
                  farmer.getId(), startDate, endDate
          );

  BigDecimal income = transactions.stream()
          .filter(transaction ->
                  "INCOME".equals(transaction.getTransactionType()))
          .map(FarmerTransaction::getAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

  BigDecimal expenses = transactions.stream()
          .filter(transaction ->
                  "EXPENSE".equals(transaction.getTransactionType()))
          .map(FarmerTransaction::getAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

  return Map.of(
          "start", startDate,
          "end", endDate,
          "income", income,
          "expenses", expenses,
          "netBalance", income.subtract(expenses),
          "count", transactions.size()
  );
 }

 private FarmerTransaction owned(Long id, Long farmerId) {
  return repo.findByIdAndFarmer_Id(id, farmerId)
          .orElseThrow(() -> new ResponseStatusException(
                  HttpStatus.NOT_FOUND,
                  "Transaction not found"
          ));
 }

 private void apply(
         FarmerTransaction transaction,
         FarmerTransactionRequest request) {

  transaction.setTransactionType(
          request.getTransactionType().toUpperCase()
  );

  transaction.setCategory(request.getCategory().trim());
  transaction.setAmount(request.getAmount());
  transaction.setTransactionDate(request.getTransactionDate());
  transaction.setDescription(request.getDescription());
  transaction.setFarmOrCrop(request.getFarmOrCrop());
 }

 private Map<String, Object> dto(FarmerTransaction transaction) {
  Map<String, Object> result = new LinkedHashMap<>();

  result.put("id", transaction.getId());
  result.put("transactionType", transaction.getTransactionType());
  result.put("category", transaction.getCategory());
  result.put("amount", transaction.getAmount());
  result.put("transactionDate", transaction.getTransactionDate());
  result.put("description", transaction.getDescription());
  result.put("farmOrCrop", transaction.getFarmOrCrop());

  return result;
 }
}