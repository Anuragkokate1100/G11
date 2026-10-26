package com.example.trackbill.AIBackend.controller;

import com.example.trackbill.AIBackend.dto.ApiResponse;
import com.example.trackbill.AIBackend.dto.LabourPaymentCalculationRequest;
import com.example.trackbill.AIBackend.dto.LabourPaymentCalculationResponse;
import com.example.trackbill.AIBackend.dto.SaveLabourPaymentRequest;
import com.example.trackbill.AIBackend.model.LabourPayment;
import com.example.trackbill.AIBackend.service.LabourPaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labour-payments")
public class LabourPaymentController {

    private final LabourPaymentService paymentService;

    public LabourPaymentController(LabourPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<LabourPaymentCalculationResponse>> calculatePayments(
            @Valid @RequestBody LabourPaymentCalculationRequest request) {
        LabourPaymentCalculationResponse calculation = paymentService.calculatePayments(request);
        return ResponseEntity.ok(ApiResponse.ok("Labour payment distribution calculated successfully", calculation));
    }

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<List<LabourPayment>>> savePayments(
            @Valid @RequestBody SaveLabourPaymentRequest request) {
        List<LabourPayment> saved = paymentService.saveLabourPayments(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Labour payment records saved and advances updated successfully", saved));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<LabourPayment>>> getPaymentHistory() {
        List<LabourPayment> history = paymentService.getLabourPaymentHistory();
        return ResponseEntity.ok(ApiResponse.ok("Labour payment history retrieved", history));
    }
}
