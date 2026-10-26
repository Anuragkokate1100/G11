package com.example.trackbill.AIBackend.controller;

import com.example.trackbill.AIBackend.dto.ApiResponse;
import com.example.trackbill.AIBackend.dto.TripRequest;
import com.example.trackbill.AIBackend.dto.TripResponse;
import com.example.trackbill.AIBackend.service.TripService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TripResponse>>> getAllTrips() {
        List<TripResponse> trips = tripService.getAllTrips();
        return ResponseEntity.ok(ApiResponse.ok("Trips retrieved successfully", trips));
    }

    @GetMapping("/unbilled")
    public ResponseEntity<ApiResponse<List<TripResponse>>> getUnbilledTrips() {
        List<TripResponse> unbilled = tripService.getUnbilledTrips();
        return ResponseEntity.ok(ApiResponse.ok("Unbilled trips retrieved", unbilled));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TripResponse>> getTripById(@PathVariable Long id) {
        TripResponse trip = tripService.getTripById(id);
        return ResponseEntity.ok(ApiResponse.ok("Trip retrieved successfully", trip));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TripResponse>> createTrip(@Valid @RequestBody TripRequest request) {
        TripResponse created = tripService.createTrip(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Trip recorded successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TripResponse>> updateTrip(@PathVariable Long id, @Valid @RequestBody TripRequest request) {
        TripResponse updated = tripService.updateTrip(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Trip updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTrip(@PathVariable Long id) {
        tripService.deleteTrip(id);
        return ResponseEntity.ok(ApiResponse.ok("Trip deleted successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<TripResponse>>> searchTrips(@RequestParam(required = false) String query) {
        List<TripResponse> results = tripService.searchTrips(query);
        return ResponseEntity.ok(ApiResponse.ok("Trip search results", results));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<TripResponse>>> filterTrips(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<TripResponse> results = tripService.filterTripsByDate(start, end);
        return ResponseEntity.ok(ApiResponse.ok("Filtered trips retrieved", results));
    }
}
