package com.example.trackbill.AIBackend.service;

import com.example.trackbill.AIBackend.dto.TripRequest;
import com.example.trackbill.AIBackend.dto.TripResponse;
import com.example.trackbill.AIBackend.exception.BadRequestException;
import com.example.trackbill.AIBackend.exception.ResourceNotFoundException;
import com.example.trackbill.AIBackend.model.TractorOwner;
import com.example.trackbill.AIBackend.model.Trip;
import com.example.trackbill.AIBackend.repository.TripRepository;
import com.example.trackbill.AIBackend.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TripService {

    private final TripRepository tripRepository;

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    public List<TripResponse> getAllTrips() {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        return tripRepository.findByTractorOwnerIdOrderByTripDateDesc(ownerId)
                .stream()
                .map(TripResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TripResponse> getUnbilledTrips() {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        return tripRepository.findByTractorOwnerIdAndIsBilledFalseOrderByTripDateDesc(ownerId)
                .stream()
                .map(TripResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public TripResponse getTripById(Long id) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        Trip trip = tripRepository.findByIdAndTractorOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip record not found with ID: " + id));
        return TripResponse.fromEntity(trip);
    }

    @Transactional
    public TripResponse createTrip(TripRequest request) {
        TractorOwner currentOwner = SecurityUtil.getCurrentOwner();

        if (request.getWeight() == null || request.getWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Sugarcane weight must be greater than 0");
        }

        if (request.getDieselRequired() != null && request.getDieselRequired().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Diesel required cannot be negative");
        }

        String tractorNum = request.getTractorNumber();
        if (tractorNum == null || tractorNum.isBlank()) {
            tractorNum = currentOwner.getTractorNumber();
        } else {
            tractorNum = tractorNum.trim().toUpperCase();
        }

        Trip trip = new Trip();
        trip.setSugarcaneOwnerName(request.getSugarcaneOwnerName().trim());
        trip.setWeight(request.getWeight());
        trip.setDieselRequired(request.getDieselRequired() != null ? request.getDieselRequired() : BigDecimal.ZERO);
        trip.setMillName(request.getMillName().trim());
        trip.setTripDate(request.getTripDate());
        trip.setTractorNumber(tractorNum);
        trip.setIsBilled(false);
        trip.setTractorOwner(currentOwner);

        Trip saved = tripRepository.save(trip);
        return TripResponse.fromEntity(saved);
    }

    @Transactional
    public TripResponse updateTrip(Long id, TripRequest request) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        Trip trip = tripRepository.findByIdAndTractorOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip record not found with ID: " + id));

        if (request.getWeight() == null || request.getWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Sugarcane weight must be greater than 0");
        }

        if (request.getDieselRequired() != null && request.getDieselRequired().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Diesel required cannot be negative");
        }

        trip.setSugarcaneOwnerName(request.getSugarcaneOwnerName().trim());
        trip.setWeight(request.getWeight());
        trip.setDieselRequired(request.getDieselRequired() != null ? request.getDieselRequired() : BigDecimal.ZERO);
        trip.setMillName(request.getMillName().trim());
        trip.setTripDate(request.getTripDate());

        if (request.getTractorNumber() != null && !request.getTractorNumber().isBlank()) {
            trip.setTractorNumber(request.getTractorNumber().trim().toUpperCase());
        }

        Trip updated = tripRepository.save(trip);
        return TripResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteTrip(Long id) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        Trip trip = tripRepository.findByIdAndTractorOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip record not found with ID: " + id));

        tripRepository.delete(trip);
    }

    public List<TripResponse> searchTrips(String query) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        if (query == null || query.isBlank()) {
            return getAllTrips();
        }
        return tripRepository.searchTrips(ownerId, query.trim())
                .stream()
                .map(TripResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<TripResponse> filterTripsByDate(LocalDate start, LocalDate end) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        if (start == null && end == null) {
            return getAllTrips();
        }
        if (start == null) start = LocalDate.of(2000, 1, 1);
        if (end == null) end = LocalDate.now();

        return tripRepository.findByTractorOwnerIdAndTripDateBetweenOrderByTripDateDesc(ownerId, start, end)
                .stream()
                .map(TripResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
