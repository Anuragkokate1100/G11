package com.example.trackbill.AIBackend.controller;

import com.example.trackbill.AIBackend.dto.ApiResponse;
import com.example.trackbill.AIBackend.dto.InvoiceEmailRequest;
import com.example.trackbill.AIBackend.service.InvoiceEmailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class InvoiceEmailController {
    private final InvoiceEmailService emailService;
    public InvoiceEmailController(InvoiceEmailService emailService) { this.emailService = emailService; }
    @PostMapping("/invoice")
    public ResponseEntity<ApiResponse<Void>> sendInvoice(@Valid @RequestBody InvoiceEmailRequest request) {
        emailService.sendInvoice(request);
        return ResponseEntity.ok(ApiResponse.ok("Invoice email sent successfully."));
    }
}
