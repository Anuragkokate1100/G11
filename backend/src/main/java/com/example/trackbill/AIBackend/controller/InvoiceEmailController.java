package com.example.trackbill.AIBackend.controller;

import com.example.trackbill.AIBackend.dto.ApiResponse;
import com.example.trackbill.AIBackend.dto.InvoiceEmailRequest;
import com.example.trackbill.AIBackend.service.InvoiceEmailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class InvoiceEmailController {

    private final InvoiceEmailService emailService;

    public InvoiceEmailController(InvoiceEmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/invoice")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendInvoice(@Valid @RequestBody InvoiceEmailRequest request) {
        Map<String, Object> result = emailService.sendInvoice(request);
        String message = (String) result.getOrDefault("message", "Invoice email sent successfully.");
        return ResponseEntity.ok(ApiResponse.ok(message, result));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getHistory() {
        return ResponseEntity.ok(ApiResponse.ok("Sent email history", emailService.getSentEmailHistory()));
    }

    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus() {
        return ResponseEntity.ok(ApiResponse.ok("Email service status", emailService.getStatus()));
    }
}
