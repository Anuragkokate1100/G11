package com.example.trackbill.AIBackend.service;

import com.example.trackbill.AIBackend.dto.BillRequest;
import com.example.trackbill.AIBackend.dto.BillResponse;
import com.example.trackbill.AIBackend.dto.TripResponse;
import com.example.trackbill.AIBackend.exception.BadRequestException;
import com.example.trackbill.AIBackend.exception.ResourceNotFoundException;
import com.example.trackbill.AIBackend.model.Billing;
import com.example.trackbill.AIBackend.model.TractorOwner;
import com.example.trackbill.AIBackend.model.Trip;
import com.example.trackbill.AIBackend.repository.BillingRepository;
import com.example.trackbill.AIBackend.repository.TractorOwnerRepository;
import com.example.trackbill.AIBackend.repository.TripRepository;
import com.example.trackbill.AIBackend.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BillingService {

    private final BillingRepository billingRepository;
    private final TripRepository tripRepository;
    private final TractorOwnerRepository ownerRepository;

    public BillingService(BillingRepository billingRepository, TripRepository tripRepository, TractorOwnerRepository ownerRepository) {
        this.billingRepository = billingRepository;
        this.tripRepository = tripRepository;
        this.ownerRepository = ownerRepository;
    }

    public List<BillResponse> getAllBills() {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        return billingRepository.findByTractorOwnerIdOrderByBillDateDesc(ownerId)
                .stream()
                .map(BillResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public BillResponse getBillById(Long id) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        Billing bill = billingRepository.findByIdAndTractorOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + id));

        BillResponse response = BillResponse.fromEntity(bill);

        // Fetch associated trips if available
        if (bill.getTripIds() != null && !bill.getTripIds().isBlank()) {
            List<Long> tripIdList = parseTripIds(bill.getTripIds());
            if (!tripIdList.isEmpty()) {
                List<TripResponse> trips = tripRepository.findByTractorOwnerIdAndIdIn(ownerId, tripIdList)
                        .stream()
                        .map(TripResponse::fromEntity)
                        .collect(Collectors.toList());
                response.setTripDetails(trips);
            }
        }

        return response;
    }

    @Transactional
    public BillResponse createBill(BillRequest request) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        TractorOwner currentOwner = ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Tractor owner not found with ID: " + ownerId));

        if (request.getRatePerTon() == null || request.getRatePerTon().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Rate per ton is required and must be greater than 0");
        }

        BigDecimal calculatedTotalWeight = BigDecimal.ZERO;
        List<Trip> selectedTrips = new ArrayList<>();
        String primaryOwnerName = request.getSugarcaneOwnerName();

        if (request.getSelectedTripIds() != null && !request.getSelectedTripIds().isEmpty()) {
            List<Long> requestedIds = request.getSelectedTripIds();
            selectedTrips = tripRepository.findByTractorOwnerIdAndIdIn(currentOwner.getId(), requestedIds);

            // Strict Owner Isolation & Validity Check: all requested trips must exist and belong to owner
            if (selectedTrips.size() != requestedIds.size()) {
                throw new BadRequestException("One or more selected trips do not exist or belong to another tractor owner");
            }

            for (Trip t : selectedTrips) {
                if (Boolean.TRUE.equals(t.getIsBilled())) {
                    throw new BadRequestException("Trip ID " + t.getId() + " has already been billed");
                }
                if (t.getWeight() == null || t.getWeight().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BadRequestException("Trip ID " + t.getId() + " has invalid sugarcane weight");
                }
                calculatedTotalWeight = calculatedTotalWeight.add(t.getWeight());
                if (primaryOwnerName == null || primaryOwnerName.isBlank()) {
                    primaryOwnerName = t.getSugarcaneOwnerName();
                }
            }
        } else if (request.getManualWeight() != null && request.getManualWeight().compareTo(BigDecimal.ZERO) > 0) {
            calculatedTotalWeight = request.getManualWeight();
        } else {
            throw new BadRequestException("Please select trips to bill or provide a valid total sugarcane weight");
        }

        if (calculatedTotalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Total sugarcane weight must be greater than 0");
        }

        // BACKEND CALCULATION: Total Amount = Total Weight * Rate Per Ton
        BigDecimal totalAmount = calculatedTotalWeight.multiply(request.getRatePerTon()).setScale(2, RoundingMode.HALF_UP);

        String tripIdsStr = selectedTrips.stream()
                .map(t -> String.valueOf(t.getId()))
                .collect(Collectors.joining(","));

        Billing bill = new Billing();
        bill.setTractorOwner(currentOwner);
        bill.setSugarcaneOwnerName(primaryOwnerName != null && !primaryOwnerName.isBlank() ? primaryOwnerName.trim() : "Sugarcane Mill & Farmer");
        bill.setTotalWeight(calculatedTotalWeight);
        bill.setRatePerTon(request.getRatePerTon());
        bill.setTotalAmount(totalAmount);
        bill.setBillDate(request.getBillDate() != null ? request.getBillDate() : LocalDate.now());
        bill.setTripIds(tripIdsStr);
        bill.setTripCount(selectedTrips.size());

        Billing savedBill = billingRepository.save(bill);

        // Mark associated trips as billed
        for (Trip t : selectedTrips) {
            t.setIsBilled(true);
            t.setBillingId(savedBill.getId());
            tripRepository.save(t);
        }

        BillResponse response = BillResponse.fromEntity(savedBill);
        response.setTripDetails(selectedTrips.stream().map(TripResponse::fromEntity).collect(Collectors.toList()));
        return response;
    }

    @Transactional
    public void deleteBill(Long id) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        Billing bill = billingRepository.findByIdAndTractorOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + id));

        // Unlink associated trips
        if (bill.getTripIds() != null && !bill.getTripIds().isBlank()) {
            List<Long> tripIds = parseTripIds(bill.getTripIds());
            if (!tripIds.isEmpty()) {
                List<Trip> trips = tripRepository.findByTractorOwnerIdAndIdIn(ownerId, tripIds);
                for (Trip t : trips) {
                    t.setIsBilled(false);
                    t.setBillingId(null);
                    tripRepository.save(t);
                }
            }
        }

        billingRepository.delete(bill);
    }

    public List<BillResponse> searchBills(String query) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        if (query == null || query.isBlank()) {
            return getAllBills();
        }
        return billingRepository.searchBills(ownerId, query.trim())
                .stream()
                .map(BillResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private List<Long> parseTripIds(String tripIdsStr) {
        List<Long> ids = new ArrayList<>();
        if (tripIdsStr == null || tripIdsStr.isBlank()) return ids;
        for (String part : tripIdsStr.split(",")) {
            try {
                ids.add(Long.parseLong(part.trim()));
            } catch (NumberFormatException ignored) {}
        }
        return ids;
    }
}
