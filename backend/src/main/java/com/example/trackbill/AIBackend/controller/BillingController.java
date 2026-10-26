package com.example.trackbill.AIBackend.controller;

import com.example.trackbill.AIBackend.dto.ApiResponse;
import com.example.trackbill.AIBackend.dto.BillRequest;
import com.example.trackbill.AIBackend.dto.BillResponse;
import com.example.trackbill.AIBackend.service.BillingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/bills", "/api/billing"})
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BillResponse>>> getAllBills() {
        List<BillResponse> bills = billingService.getAllBills();
        return ResponseEntity.ok(ApiResponse.ok("Bills retrieved successfully", bills));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> getBillById(@PathVariable Long id) {
        BillResponse bill = billingService.getBillById(id);
        return ResponseEntity.ok(ApiResponse.ok("Bill details retrieved", bill));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BillResponse>> createBill(@Valid @RequestBody BillRequest request) {
        BillResponse created = billingService.createBill(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Bill created successfully", created));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBill(@PathVariable Long id) {
        billingService.deleteBill(id);
        return ResponseEntity.ok(ApiResponse.ok("Bill deleted and associated trips released successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BillResponse>>> searchBills(@RequestParam(required = false) String query) {
        List<BillResponse> results = billingService.searchBills(query);
        return ResponseEntity.ok(ApiResponse.ok("Bill search results", results));
    }
}
