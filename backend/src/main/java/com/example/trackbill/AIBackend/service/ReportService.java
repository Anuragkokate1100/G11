package com.example.trackbill.AIBackend.service;

import com.example.trackbill.AIBackend.dto.BillResponse;
import com.example.trackbill.AIBackend.dto.ExpenseResponse;
import com.example.trackbill.AIBackend.dto.ReportSummaryResponse;
import com.example.trackbill.AIBackend.dto.TripResponse;
import com.example.trackbill.AIBackend.model.ExpenseType;
import com.example.trackbill.AIBackend.repository.BillingRepository;
import com.example.trackbill.AIBackend.repository.ExpenseRepository;
import com.example.trackbill.AIBackend.repository.TripRepository;
import com.example.trackbill.AIBackend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final TripRepository tripRepository;
    private final ExpenseRepository expenseRepository;
    private final BillingRepository billingRepository;

    public ReportService(TripRepository tripRepository,
                         ExpenseRepository expenseRepository,
                         BillingRepository billingRepository) {
        this.tripRepository = tripRepository;
        this.expenseRepository = expenseRepository;
        this.billingRepository = billingRepository;
    }

    public ReportSummaryResponse generateReport(String filter, LocalDate customStart, LocalDate customEnd) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();

        LocalDate[] range = resolveReportDates(filter, customStart, customEnd);
        LocalDate start = range[0];
        LocalDate end = range[1];

        long totalTrips;
        BigDecimal totalWeight;
        BigDecimal totalRevenue;
        BigDecimal totalExpenses;
        BigDecimal dieselExpenses;
        BigDecimal labourExpenses;
        BigDecimal otherExpenses;

        if (start == null) {
            totalTrips = tripRepository.countByTractorOwnerId(ownerId);
            totalWeight = tripRepository.sumWeightByOwnerId(ownerId);
            totalRevenue = billingRepository.sumRevenueByOwnerId(ownerId);
            totalExpenses = expenseRepository.sumAmountByOwnerId(ownerId);
            dieselExpenses = expenseRepository.sumAmountByOwnerIdAndExpenseType(ownerId, ExpenseType.DIESEL);
            labourExpenses = expenseRepository.sumAmountByOwnerIdAndExpenseType(ownerId, ExpenseType.LABOUR);
        } else {
            totalTrips = tripRepository.countByTractorOwnerIdAndTripDateBetween(ownerId, start, end);
            totalWeight = tripRepository.sumWeightByOwnerIdAndTripDateBetween(ownerId, start, end);
            totalRevenue = billingRepository.sumRevenueByOwnerIdAndBillDateBetween(ownerId, start, end);
            totalExpenses = expenseRepository.sumAmountByOwnerIdAndExpenseDateBetween(ownerId, start, end);
            dieselExpenses = expenseRepository.sumAmountByOwnerIdAndExpenseTypeAndExpenseDateBetween(ownerId, ExpenseType.DIESEL, start, end);
            labourExpenses = expenseRepository.sumAmountByOwnerIdAndExpenseTypeAndExpenseDateBetween(ownerId, ExpenseType.LABOUR, start, end);
        }

        otherExpenses = totalExpenses.subtract(dieselExpenses).subtract(labourExpenses);
        if (otherExpenses.compareTo(BigDecimal.ZERO) < 0) {
            otherExpenses = BigDecimal.ZERO;
        }

        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);

        // Fetch tabular items
        List<TripResponse> trips;
        List<ExpenseResponse> expenses;
        List<BillResponse> bills;

        if (start == null) {
            trips = tripRepository.findByTractorOwnerIdOrderByTripDateDesc(ownerId)
                    .stream().map(TripResponse::fromEntity).collect(Collectors.toList());
            expenses = expenseRepository.findByTractorOwnerIdOrderByExpenseDateDesc(ownerId)
                    .stream().map(ExpenseResponse::fromEntity).collect(Collectors.toList());
            bills = billingRepository.findByTractorOwnerIdOrderByBillDateDesc(ownerId)
                    .stream().map(BillResponse::fromEntity).collect(Collectors.toList());
        } else {
            trips = tripRepository.findByTractorOwnerIdAndTripDateBetweenOrderByTripDateDesc(ownerId, start, end)
                    .stream().map(TripResponse::fromEntity).collect(Collectors.toList());
            expenses = expenseRepository.findByTractorOwnerIdAndExpenseDateBetweenOrderByExpenseDateDesc(ownerId, start, end)
                    .stream().map(ExpenseResponse::fromEntity).collect(Collectors.toList());
            bills = billingRepository.findByTractorOwnerIdAndBillDateBetweenOrderByBillDateDesc(ownerId, start, end)
                    .stream().map(BillResponse::fromEntity).collect(Collectors.toList());
        }

        ReportSummaryResponse report = new ReportSummaryResponse();
        report.setFilterType(filter != null ? filter.toUpperCase() : "ALL");
        report.setStartDate(start);
        report.setEndDate(end);
        report.setTotalTrips(totalTrips);
        report.setTotalWeight(totalWeight.setScale(2, RoundingMode.HALF_UP));
        report.setTotalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP));
        report.setTotalExpenses(totalExpenses.setScale(2, RoundingMode.HALF_UP));
        report.setDieselExpenses(dieselExpenses.setScale(2, RoundingMode.HALF_UP));
        report.setLabourExpenses(labourExpenses.setScale(2, RoundingMode.HALF_UP));
        report.setOtherExpenses(otherExpenses.setScale(2, RoundingMode.HALF_UP));
        report.setNetProfit(netProfit.setScale(2, RoundingMode.HALF_UP));
        report.setTrips(trips);
        report.setExpenses(expenses);
        report.setBills(bills);

        return report;
    }

    private LocalDate[] resolveReportDates(String filter, LocalDate customStart, LocalDate customEnd) {
        if (customStart != null || customEnd != null) {
            LocalDate start = customStart != null ? customStart : LocalDate.of(2000, 1, 1);
            LocalDate end = customEnd != null ? customEnd : LocalDate.now();
            return new LocalDate[]{start, end};
        }

        if (filter == null || filter.equalsIgnoreCase("ALL")) {
            return new LocalDate[]{null, null};
        }

        LocalDate now = LocalDate.now();
        switch (filter.toUpperCase()) {
            case "TODAY":
                return new LocalDate[]{now, now};
            case "LAST_7_DAYS":
            case "7D":
                return new LocalDate[]{now.minusDays(7), now};
            case "LAST_30_DAYS":
            case "30D":
                return new LocalDate[]{now.minusDays(30), now};
            case "CURRENT_MONTH":
            case "MONTH":
                return new LocalDate[]{now.withDayOfMonth(1), now};
            case "PREVIOUS_MONTH":
                YearMonth prev = YearMonth.now().minusMonths(1);
                return new LocalDate[]{prev.atDay(1), prev.atEndOfMonth()};
            case "CURRENT_YEAR":
            case "YEAR":
                return new LocalDate[]{now.withDayOfYear(1), now};
            default:
                return new LocalDate[]{null, null};
        }
    }
}
