package com.example.trackbill.AIBackend.controller;

import com.example.trackbill.AIBackend.dto.ApiResponse;
import com.example.trackbill.AIBackend.dto.LabourRequest;
import com.example.trackbill.AIBackend.dto.LabourResponse;
import com.example.trackbill.AIBackend.dto.LabourPaymentCalculationRequest;
import com.example.trackbill.AIBackend.dto.LabourPaymentCalculationResponse;
import com.example.trackbill.AIBackend.service.LabourPaymentService;
import com.example.trackbill.AIBackend.service.LabourService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labour")
public class LabourController {

    private final LabourService labourService;
    private final LabourPaymentService labourPaymentService;

    public LabourController(LabourService labourService, LabourPaymentService labourPaymentService) {
        this.labourService = labourService;
        this.labourPaymentService = labourPaymentService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<LabourPaymentCalculationResponse>> calculatePayments(
            @Valid @RequestBody LabourPaymentCalculationRequest request) {
        LabourPaymentCalculationResponse calculation = labourPaymentService.calculatePayments(request);
        return ResponseEntity.ok(ApiResponse.ok("Labour payment distribution calculated successfully", calculation));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LabourResponse>>> getAllLabour() {
        List<LabourResponse> labourList = labourService.getAllLabour();
        return ResponseEntity.ok(ApiResponse.ok("Labour list retrieved", labourList));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LabourResponse>> getLabourById(@PathVariable Long id) {
        LabourResponse labour = labourService.getLabourById(id);
        return ResponseEntity.ok(ApiResponse.ok("Labour record retrieved", labour));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LabourResponse>> createLabour(@Valid @RequestBody LabourRequest request) {
        LabourResponse created = labourService.createLabour(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Labour added successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LabourResponse>> updateLabour(@PathVariable Long id, @Valid @RequestBody LabourRequest request) {
        LabourResponse updated = labourService.updateLabour(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Labour updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLabour(@PathVariable Long id) {
        labourService.deleteLabour(id);
        return ResponseEntity.ok(ApiResponse.ok("Labour deleted successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<LabourResponse>>> searchLabour(@RequestParam(required = false) String query) {
        List<LabourResponse> results = labourService.searchLabour(query);
        return ResponseEntity.ok(ApiResponse.ok("Labour search results", results));
    }
}
