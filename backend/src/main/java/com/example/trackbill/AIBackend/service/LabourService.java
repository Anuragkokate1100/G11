package com.example.trackbill.AIBackend.service;

import com.example.trackbill.AIBackend.dto.LabourRequest;
import com.example.trackbill.AIBackend.dto.LabourResponse;
import com.example.trackbill.AIBackend.exception.BadRequestException;
import com.example.trackbill.AIBackend.exception.ResourceNotFoundException;
import com.example.trackbill.AIBackend.model.Labour;
import com.example.trackbill.AIBackend.model.TractorOwner;
import com.example.trackbill.AIBackend.repository.LabourRepository;
import com.example.trackbill.AIBackend.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabourService {

    private final LabourRepository labourRepository;

    public LabourService(LabourRepository labourRepository) {
        this.labourRepository = labourRepository;
    }

    public List<LabourResponse> getAllLabour() {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        return labourRepository.findByTractorOwnerIdOrderByNameAsc(ownerId)
                .stream()
                .map(LabourResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public LabourResponse getLabourById(Long id) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        Labour labour = labourRepository.findByIdAndTractorOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Labour record not found with ID: " + id));
        return LabourResponse.fromEntity(labour);
    }

    @Transactional
    public LabourResponse createLabour(LabourRequest request) {
        TractorOwner currentOwner = SecurityUtil.getCurrentOwner();

        if (request.getAdvanceMoney() != null && request.getAdvanceMoney().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Advance money cannot be negative");
        }

        Labour labour = new Labour();
        labour.setName(request.getName().trim());
        labour.setVillage(request.getVillage().trim());
        labour.setCategory(request.getCategory());
        labour.setPhone(request.getPhone().trim());
        labour.setAdvanceMoney(request.getAdvanceMoney() != null ? request.getAdvanceMoney() : BigDecimal.ZERO);
        labour.setType(request.getType());
        labour.setTractorOwner(currentOwner);

        Labour saved = labourRepository.save(labour);
        return LabourResponse.fromEntity(saved);
    }

    @Transactional
    public LabourResponse updateLabour(Long id, LabourRequest request) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        Labour labour = labourRepository.findByIdAndTractorOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Labour record not found with ID: " + id));

        if (request.getAdvanceMoney() != null && request.getAdvanceMoney().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Advance money cannot be negative");
        }

        labour.setName(request.getName().trim());
        labour.setVillage(request.getVillage().trim());
        labour.setCategory(request.getCategory());
        labour.setPhone(request.getPhone().trim());
        if (request.getAdvanceMoney() != null) {
            labour.setAdvanceMoney(request.getAdvanceMoney());
        }
        labour.setType(request.getType());

        Labour updated = labourRepository.save(labour);
        return LabourResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteLabour(Long id) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        Labour labour = labourRepository.findByIdAndTractorOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Labour record not found with ID: " + id));

        labourRepository.delete(labour);
    }

    public List<LabourResponse> searchLabour(String query) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        if (query == null || query.isBlank()) {
            return getAllLabour();
        }
        return labourRepository.searchLabour(ownerId, query.trim())
                .stream()
                .map(LabourResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
