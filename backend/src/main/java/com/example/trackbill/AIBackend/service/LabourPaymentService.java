package com.example.trackbill.AIBackend.service;

import com.example.trackbill.AIBackend.dto.LabourPaymentCalculationRequest;
import com.example.trackbill.AIBackend.dto.LabourPaymentCalculationResponse;
import com.example.trackbill.AIBackend.dto.LabourPaymentItemDto;
import com.example.trackbill.AIBackend.dto.SaveLabourPaymentRequest;
import com.example.trackbill.AIBackend.exception.BadRequestException;
import com.example.trackbill.AIBackend.model.*;
import com.example.trackbill.AIBackend.repository.LabourPaymentRepository;
import com.example.trackbill.AIBackend.repository.LabourRepository;
import com.example.trackbill.AIBackend.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * LabourPaymentService implements the specialized sugarcane harvesting
 * Koyta payment calculation algorithm for Maharashtra tractor operations.
 *
 * Algorithm Rules (Section 19):
 * 1. Workers belong to FULL_KOYTA or HALF_KOYTA categories.
 * 2. Total workers = N_total = N_full + N_half.
 * 3. Base share per worker = Total Weight / N_total.
 * 4. Each Full Koyta worker initially assigned base share: W_base.
 * 5. Each Half Koyta worker assigned half of base share: W_half = 0.5 * W_base.
 * 6. Distributed weight = (N_full * W_base) + (N_half * W_half).
 * 7. Remaining weight = Total Weight - Distributed weight.
 * 8. Remaining weight is redistributed evenly among Full Koyta workers:
 *    Redistribution per Full Koyta = Remaining / N_full.
 * 9. Final Full Koyta share = W_base + (Remaining / N_full).
 * 10. Final Half Koyta share = W_half = 0.5 * W_base.
 * 11. Gross Pay = Assigned Weight * Rate Per Ton.
 * 12. Advance Deducted = min(Current Advance, Gross Pay).
 * 13. Final Net Payable = Gross Pay - Advance Deducted.
 */
@Service
public class LabourPaymentService {

    private final LabourRepository labourRepository;
    private final LabourPaymentRepository paymentRepository;

    public LabourPaymentService(LabourRepository labourRepository, LabourPaymentRepository paymentRepository) {
        this.labourRepository = labourRepository;
        this.paymentRepository = paymentRepository;
    }

    public LabourPaymentCalculationResponse calculatePayments(LabourPaymentCalculationRequest request) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();

        if (request.getTotalWeight() == null || request.getTotalWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Sugarcane weight must be greater than zero");
        }
        if (request.getRatePerTon() == null || request.getRatePerTon().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Rate per ton must be greater than zero");
        }

        // Fetch selected or all active labour for this owner
        List<Labour> labourers;
        if (request.getLabourIds() != null && !request.getLabourIds().isEmpty()) {
            labourers = new ArrayList<>();
            for (Long id : request.getLabourIds()) {
                labourRepository.findByIdAndTractorOwnerId(id, ownerId).ifPresent(labourers::add);
            }
        } else {
            labourers = labourRepository.findByTractorOwnerIdOrderByNameAsc(ownerId);
        }

        if (labourers.isEmpty()) {
            throw new BadRequestException("No labour records found to calculate payments for");
        }

        BigDecimal totalWeight = request.getTotalWeight();
        BigDecimal ratePerTon = request.getRatePerTon();

        int fullCount = 0;
        int halfCount = 0;

        for (Labour l : labourers) {
            if (l.getCategory() == LabourCategory.HALF_KOYTA) {
                halfCount++;
            } else {
                fullCount++;
            }
        }

        int totalWorkers = fullCount + halfCount;

        // Step 1: Base share = Total Weight / Total Workers
        BigDecimal baseShare = totalWeight.divide(BigDecimal.valueOf(totalWorkers), 6, RoundingMode.HALF_UP);

        BigDecimal fullKoytaFinalWeight;
        BigDecimal halfKoytaFinalWeight;

        if (fullCount == 0) {
            // Only Half Koytas present: share evenly
            halfKoytaFinalWeight = totalWeight.divide(BigDecimal.valueOf(halfCount), 3, RoundingMode.HALF_UP);
            fullKoytaFinalWeight = BigDecimal.ZERO;
        } else if (halfCount == 0) {
            // Only Full Koytas present: share evenly
            fullKoytaFinalWeight = totalWeight.divide(BigDecimal.valueOf(fullCount), 3, RoundingMode.HALF_UP);
            halfKoytaFinalWeight = BigDecimal.ZERO;
        } else {
            // Both present:
            // Initial Full Koyta = baseShare
            // Initial Half Koyta = 0.5 * baseShare
            BigDecimal halfKoytaBase = baseShare.multiply(new BigDecimal("0.5")).setScale(6, RoundingMode.HALF_UP);

            BigDecimal initialDistributed = baseShare.multiply(BigDecimal.valueOf(fullCount))
                    .add(halfKoytaBase.multiply(BigDecimal.valueOf(halfCount)));

            BigDecimal remaining = totalWeight.subtract(initialDistributed);

            // Redistribute remaining weight among Full Koyta workers
            BigDecimal bonusPerFull = remaining.divide(BigDecimal.valueOf(fullCount), 6, RoundingMode.HALF_UP);

            fullKoytaFinalWeight = baseShare.add(bonusPerFull).setScale(3, RoundingMode.HALF_UP);
            halfKoytaFinalWeight = halfKoytaBase.setScale(3, RoundingMode.HALF_UP);
        }

        // Build individual worker calculation rows
        List<LabourPaymentItemDto> items = new ArrayList<>();
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalAdvanceDeducted = BigDecimal.ZERO;
        BigDecimal totalNetPayable = BigDecimal.ZERO;

        for (Labour l : labourers) {
            BigDecimal assignedWeight = (l.getCategory() == LabourCategory.HALF_KOYTA) ? halfKoytaFinalWeight : fullKoytaFinalWeight;
            BigDecimal gross = assignedWeight.multiply(ratePerTon).setScale(2, RoundingMode.HALF_UP);

            BigDecimal advance = l.getAdvanceMoney() != null ? l.getAdvanceMoney() : BigDecimal.ZERO;
            // Advance deducted cannot exceed gross earnings
            BigDecimal advanceDeducted = advance.min(gross).setScale(2, RoundingMode.HALF_UP);
            BigDecimal netPayable = gross.subtract(advanceDeducted).setScale(2, RoundingMode.HALF_UP);

            totalGross = totalGross.add(gross);
            totalAdvanceDeducted = totalAdvanceDeducted.add(advanceDeducted);
            totalNetPayable = totalNetPayable.add(netPayable);

            LabourPaymentItemDto item = new LabourPaymentItemDto(
                    l.getId(),
                    l.getName(),
                    l.getVillage(),
                    l.getCategory(),
                    l.getType(),
                    assignedWeight,
                    ratePerTon,
                    gross,
                    advance,
                    advanceDeducted,
                    netPayable
            );
            items.add(item);
        }

        LabourPaymentCalculationResponse response = new LabourPaymentCalculationResponse();
        response.setTotalWeight(totalWeight);
        response.setRatePerTon(ratePerTon);
        response.setTotalWorkers(totalWorkers);
        response.setFullKoytaCount(fullCount);
        response.setHalfKoytaCount(halfCount);
        response.setBaseSharePerWorker(baseShare.setScale(3, RoundingMode.HALF_UP));
        response.setFullKoytaShareWeight(fullKoytaFinalWeight);
        response.setHalfKoytaShareWeight(halfKoytaFinalWeight);
        response.setTotalCalculatedGross(totalGross);
        response.setTotalAdvanceDeducted(totalAdvanceDeducted);
        response.setTotalNetPayable(totalNetPayable);
        response.setItems(items);

        return response;
    }

    @Transactional
    public List<LabourPayment> saveLabourPayments(SaveLabourPaymentRequest request) {
        TractorOwner currentOwner = SecurityUtil.getCurrentOwner();

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("No payment items to save");
        }

        LocalDate date = request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now();
        List<LabourPayment> savedList = new ArrayList<>();

        for (LabourPaymentItemDto item : request.getItems()) {
            Labour labour = labourRepository.findByIdAndTractorOwnerId(item.getLabourId(), currentOwner.getId())
                    .orElseThrow(() -> new BadRequestException("Labour record not found: ID " + item.getLabourId()));

            LabourPayment payment = new LabourPayment();
            payment.setLabourId(labour.getId());
            payment.setLabourName(labour.getName());
            payment.setCategory(labour.getCategory());
            payment.setBillingId(request.getBillingId());
            payment.setAssignedWeight(item.getAssignedWeight());
            payment.setRatePerTon(item.getRatePerTon());
            payment.setCalculatedAmount(item.getGrossAmount());
            payment.setAdvanceDeducted(item.getAdvanceDeducted() != null ? item.getAdvanceDeducted() : BigDecimal.ZERO);
            payment.setFinalAmount(item.getFinalAmount());
            payment.setPaymentDate(date);
            payment.setTractorOwner(currentOwner);

            savedList.add(paymentRepository.save(payment));

            // Deduct advance from worker's profile
            if (item.getAdvanceDeducted() != null && item.getAdvanceDeducted().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal remainingAdvance = labour.getAdvanceMoney().subtract(item.getAdvanceDeducted());
                if (remainingAdvance.compareTo(BigDecimal.ZERO) < 0) {
                    remainingAdvance = BigDecimal.ZERO;
                }
                labour.setAdvanceMoney(remainingAdvance);
                labourRepository.save(labour);
            }
        }

        return savedList;
    }

    public List<LabourPayment> getLabourPaymentHistory() {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        return paymentRepository.findByTractorOwnerIdOrderByPaymentDateDesc(ownerId);
    }
}
