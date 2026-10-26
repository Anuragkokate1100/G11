package com.example.trackbill.AIBackend.controller;

import com.example.trackbill.AIBackend.dto.*;
import com.example.trackbill.AIBackend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful!", response));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful!", response));
    }

    @GetMapping("/owner/profile")
    public ResponseEntity<ApiResponse<OwnerProfileDto>> getProfile() {
        OwnerProfileDto profile = authService.getProfile();
        return ResponseEntity.ok(ApiResponse.ok("Owner profile retrieved", profile));
    }

    @PutMapping("/owner/profile")
    public ResponseEntity<ApiResponse<OwnerProfileDto>> updateProfile(@RequestBody OwnerProfileDto dto) {
        OwnerProfileDto updated = authService.updateProfile(dto);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated successfully", updated));
    }
}
