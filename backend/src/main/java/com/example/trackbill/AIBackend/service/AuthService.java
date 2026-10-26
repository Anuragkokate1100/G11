package com.example.trackbill.AIBackend.service;

import com.example.trackbill.AIBackend.dto.AuthResponse;
import com.example.trackbill.AIBackend.dto.LoginRequest;
import com.example.trackbill.AIBackend.dto.OwnerProfileDto;
import com.example.trackbill.AIBackend.dto.RegisterRequest;
import com.example.trackbill.AIBackend.exception.BadRequestException;
import com.example.trackbill.AIBackend.exception.ResourceNotFoundException;
import com.example.trackbill.AIBackend.model.TractorOwner;
import com.example.trackbill.AIBackend.repository.TractorOwnerRepository;
import com.example.trackbill.AIBackend.security.JwtUtil;
import com.example.trackbill.AIBackend.util.SecurityUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final TractorOwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(TractorOwnerRepository ownerRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.ownerRepository = ownerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();

        if (ownerRepository.existsByEmail(cleanEmail)) {
            throw new BadRequestException("An account with email " + cleanEmail + " already exists. Please login instead.");
        }

        String cleanTractorNumber = request.getTractorNumber().trim().toUpperCase();

        TractorOwner owner = new TractorOwner();
        owner.setName(request.getName().trim());
        owner.setTractorNumber(cleanTractorNumber);
        owner.setTrollyCount(request.getTrollyCount());
        owner.setEmail(cleanEmail);
        owner.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        owner.setVillage(request.getVillage().trim());
        owner.setTaluka(request.getTaluka().trim());
        owner.setDistrict(request.getDistrict().trim());
        owner.setPincode(request.getPincode().trim());

        TractorOwner savedOwner = ownerRepository.save(owner);

        String token = jwtUtil.generateToken(savedOwner.getEmail(), savedOwner.getId(), savedOwner.getName());
        return new AuthResponse(true, "Tractor owner registered successfully!", token, OwnerProfileDto.fromEntity(savedOwner));
    }

    public AuthResponse login(LoginRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();

        TractorOwner owner = ownerRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword().trim(), owner.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(owner.getEmail(), owner.getId(), owner.getName());
        return new AuthResponse(true, "Login successful! Welcome back, " + owner.getName(), token, OwnerProfileDto.fromEntity(owner));
    }

    public OwnerProfileDto getProfile() {
        TractorOwner currentOwner = SecurityUtil.getCurrentOwner();
        return OwnerProfileDto.fromEntity(currentOwner);
    }

    @Transactional
    public OwnerProfileDto updateProfile(OwnerProfileDto dto) {
        TractorOwner currentOwner = SecurityUtil.getCurrentOwner();

        TractorOwner owner = ownerRepository.findById(currentOwner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner account not found"));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            owner.setName(dto.getName().trim());
        }
        if (dto.getTractorNumber() != null && !dto.getTractorNumber().isBlank()) {
            owner.setTractorNumber(dto.getTractorNumber().trim().toUpperCase());
        }
        if (dto.getTrollyCount() != null && dto.getTrollyCount() >= 0) {
            owner.setTrollyCount(dto.getTrollyCount());
        }
        if (dto.getVillage() != null && !dto.getVillage().isBlank()) {
            owner.setVillage(dto.getVillage().trim());
        }
        if (dto.getTaluka() != null && !dto.getTaluka().isBlank()) {
            owner.setTaluka(dto.getTaluka().trim());
        }
        if (dto.getDistrict() != null && !dto.getDistrict().isBlank()) {
            owner.setDistrict(dto.getDistrict().trim());
        }
        if (dto.getPincode() != null && !dto.getPincode().isBlank()) {
            owner.setPincode(dto.getPincode().trim());
        }

        TractorOwner updated = ownerRepository.save(owner);
        return OwnerProfileDto.fromEntity(updated);
    }
}
