package com.example.trackbill.AIBackend.controller;

import com.example.trackbill.AIBackend.dto.*;
import com.example.trackbill.AIBackend.model.FarmCrop;
import com.example.trackbill.AIBackend.model.Farmer;
import com.example.trackbill.AIBackend.repository.FarmCropRepository;
import com.example.trackbill.AIBackend.repository.FarmerRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/farmer/crops")
public class FarmCropController {

    private final FarmCropRepository crops;
    private final FarmerRepository farmers;

    public FarmCropController(FarmCropRepository c, FarmerRepository f) {
        this.crops = c;
        this.farmers = f;
    }

    private Farmer current(Authentication a) {
        if (a == null || !(a.getPrincipal() instanceof Farmer p)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Farmer login required");
        }
        return p;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FarmCropResponse>>> list(Authentication a) {
        List<FarmCropResponse> list = crops.findAllByFarmerIdOrderByCreatedAtDesc(current(a).getId())
                .stream()
                .map(FarmCropResponse::new)
                .toList();
        return ResponseEntity.ok(ApiResponse.ok("Crops retrieved successfully", list));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FarmCropResponse>> create(@Valid @RequestBody FarmCropRequest r, Authentication a) {
        Farmer f = current(a);
        FarmCrop c = new FarmCrop();
        c.setFarmer(f);
        apply(c, r);
        FarmCropResponse response = new FarmCropResponse(crops.save(c));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Crop record saved.", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FarmCropResponse>> update(@PathVariable Long id, @Valid @RequestBody FarmCropRequest r, Authentication a) {
        FarmCrop c = crops.findByIdAndFarmerId(id, current(a).getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Crop not found"));
        apply(c, r);
        FarmCropResponse response = new FarmCropResponse(crops.save(c));
        return ResponseEntity.ok(ApiResponse.ok("Crop record updated.", response));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication a) {
        FarmCrop c = crops.findByIdAndFarmerId(id, current(a).getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Crop not found"));
        crops.delete(c);
    }

    private void apply(FarmCrop c, FarmCropRequest r) {
        c.setCropName(r.getCropName().trim());
        c.setLandArea(r.getLandArea());
        c.setAreaUnit(r.getAreaUnit());
        c.setSowingDate(r.getSowingDate());
        c.setExpectedHarvestDate(r.getExpectedHarvestDate());
        c.setStatus(r.getStatus() == null ? "GROWING" : r.getStatus());
    }
}
