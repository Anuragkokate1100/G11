package com.example.trackbill.AIBackend.service;

import com.example.trackbill.AIBackend.dto.*;
import com.example.trackbill.AIBackend.model.TractorOwner;
import com.example.trackbill.AIBackend.repository.*;
import com.example.trackbill.AIBackend.util.SecurityUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final TripRepository tripRepository;
    private final ExpenseRepository expenseRepository;
    private final BillingRepository billingRepository;
    private final LabourRepository labourRepository;

    public DashboardService(TripRepository tripRepository,
                            ExpenseRepository expenseRepository,
                            BillingRepository billingRepository,
                            LabourRepository labourRepository) {
        this.tripRepository = tripRepository;
        this.expenseRepository = expenseRepository;
        this.billingRepository = billingRepository;
        this.labourRepository = labourRepository;
    }

    public DashboardStatsResponse getDashboardStats(String filter) {
        TractorOwner owner = SecurityUtil.getCurrentOwner();
        Long ownerId = owner.getId();

        LocalDate[] dateRange = parseFilterRange(filter);
        LocalDate start = dateRange[0];
        LocalDate end = dateRange[1];

        long totalTrips;
        BigDecimal totalWeight;
        BigDecimal totalDiesel;
        BigDecimal totalExpenses;
        BigDecimal totalRevenue;

        if (start == null) {
            totalTrips = tripRepository.countByTractorOwnerId(ownerId);
            totalWeight = tripRepository.sumWeightByOwnerId(ownerId);
            totalDiesel = tripRepository.sumDieselByOwnerId(ownerId);
            totalExpenses = expenseRepository.sumAmountByOwnerId(ownerId);
            totalRevenue = billingRepository.sumRevenueByOwnerId(ownerId);
        } else {
            totalTrips = tripRepository.countByTractorOwnerIdAndTripDateBetween(ownerId, start, end);
            totalWeight = tripRepository.sumWeightByOwnerIdAndTripDateBetween(ownerId, start, end);
            totalDiesel = tripRepository.sumDieselByOwnerIdAndTripDateBetween(ownerId, start, end);
            totalExpenses = expenseRepository.sumAmountByOwnerIdAndExpenseDateBetween(ownerId, start, end);
            totalRevenue = billingRepository.sumRevenueByOwnerIdAndBillDateBetween(ownerId, start, end);
        }

        BigDecimal netProfit = totalRevenue.subtract(totalExpenses);

        long totalLabour = labourRepository.countByTractorOwnerId(ownerId);
        BigDecimal totalLabourAdvance = labourRepository.sumAdvanceMoneyByOwnerId(ownerId);

        List<TripResponse> recentTrips = tripRepository.findByTractorOwnerIdOrderByTripDateDesc(ownerId)
                .stream().limit(5).map(TripResponse::fromEntity).collect(Collectors.toList());

        List<ExpenseResponse> recentExpenses = expenseRepository.findByTractorOwnerIdOrderByExpenseDateDesc(ownerId)
                .stream().limit(5).map(ExpenseResponse::fromEntity).collect(Collectors.toList());

        DashboardStatsResponse response = new DashboardStatsResponse();
        response.setOwnerName(owner.getName());
        response.setTractorNumber(owner.getTractorNumber());
        response.setTrollyCount(owner.getTrollyCount());
        response.setTotalTrips(totalTrips);
        response.setTotalWeight(totalWeight.setScale(2, RoundingMode.HALF_UP));
        response.setTotalDiesel(totalDiesel.setScale(2, RoundingMode.HALF_UP));
        response.setTotalExpenses(totalExpenses.setScale(2, RoundingMode.HALF_UP));
        response.setTotalRevenue(totalRevenue.setScale(2, RoundingMode.HALF_UP));
        response.setNetProfit(netProfit.setScale(2, RoundingMode.HALF_UP));
        response.setTotalLabour(totalLabour);
        response.setTotalLabourAdvance(totalLabourAdvance.setScale(2, RoundingMode.HALF_UP));
        response.setRecentTrips(recentTrips);
        response.setRecentExpenses(recentExpenses);

        return response;
    }

    public Map<String, VisualizationResponse> getDashboardVisualizations(String filter) {
        Long ownerId = SecurityUtil.getCurrentOwnerId();
        Map<String, VisualizationResponse> charts = new HashMap<>();

        // 1. Monthly Sugarcane Weight Trend
        charts.put("monthlyWeight", buildMonthlyWeightChart(ownerId));

        // 2. Monthly Trips Count Trend
        charts.put("monthlyTrips", buildMonthlyTripsChart(ownerId));

        // 3. Expense Breakdown by Category
        charts.put("expenseCategory", buildExpenseCategoryChart(ownerId));

        // 4. Revenue vs Expenses Comparative Trend
        charts.put("revenueVsExpense", buildRevenueVsExpenseChart(ownerId));

        // 5. Weight Transported by Sugar Mill
        charts.put("millDistribution", buildMillDistributionChart(ownerId));

        // 6. Labour Category & Type Distribution
        charts.put("labourDistribution", buildLabourDistributionChart(ownerId));

        // 7. Monthly Net Profit Trend
        charts.put("profitTrend", buildProfitTrendChart(ownerId));

        return charts;
    }

    private VisualizationResponse buildMonthlyWeightChart(Long ownerId) {
        List<Object[]> rawTrends = tripRepository.getMonthlyTripTrends(ownerId);
        if (rawTrends.isEmpty()) {
            return VisualizationResponse.empty("bar", "Monthly Sugarcane Weight (Tons)", "No trip records available yet.");
        }

        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        for (Object[] row : rawTrends) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            BigDecimal weight = (BigDecimal) row[2];

            labels.add(getMonthName(month) + " " + year);
            data.add(weight.setScale(2, RoundingMode.HALF_UP));
        }

        ChartDatasetDto dataset = new ChartDatasetDto(
                "Sugarcane Weight (Tons)",
                data,
                "rgba(46, 125, 50, 0.75)",
                "rgba(46, 125, 50, 1)"
        );

        return new VisualizationResponse("bar", "Monthly Sugarcane Weight (Tons)", labels, List.of(dataset), true, null);
    }

    private VisualizationResponse buildMonthlyTripsChart(Long ownerId) {
        List<Object[]> rawTrends = tripRepository.getMonthlyTripTrends(ownerId);
        if (rawTrends.isEmpty()) {
            return VisualizationResponse.empty("line", "Monthly Trip Count", "No trip records available yet.");
        }

        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        for (Object[] row : rawTrends) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            long count = ((Number) row[3]).longValue();

            labels.add(getMonthName(month) + " " + year);
            data.add(count);
        }

        ChartDatasetDto dataset = new ChartDatasetDto(
                "Number of Trips",
                data,
                "rgba(230, 81, 0, 0.2)",
                "rgba(230, 81, 0, 1)",
                true
        );

        return new VisualizationResponse("line", "Monthly Trip Count", labels, List.of(dataset), true, null);
    }

    private VisualizationResponse buildExpenseCategoryChart(Long ownerId) {
        List<Object[]> rawExpenses = expenseRepository.getCategoryExpenseBreakdown(ownerId);
        if (rawExpenses.isEmpty()) {
            return VisualizationResponse.empty("doughnut", "Expense Breakdown by Category", "No expense records found.");
        }

        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        for (Object[] row : rawExpenses) {
            labels.add(row[0].toString());
            data.add(((BigDecimal) row[1]).setScale(2, RoundingMode.HALF_UP));
        }

        List<String> colors = List.of(
                "#e65100", "#1b5e20", "#0277bd", "#f57f17", "#6a1b9a", "#c2185b", "#546e7a"
        );

        ChartDatasetDto dataset = new ChartDatasetDto(
                "Amount (₹)",
                data,
                colors.subList(0, Math.min(colors.size(), labels.size())),
                "#ffffff"
        );

        return new VisualizationResponse("doughnut", "Expense Breakdown by Category", labels, List.of(dataset), true, null);
    }

    private VisualizationResponse buildRevenueVsExpenseChart(Long ownerId) {
        List<Object[]> revTrends = billingRepository.getMonthlyRevenueTrends(ownerId);
        List<Object[]> expTrends = expenseRepository.getMonthlyExpenseTrends(ownerId);

        if (revTrends.isEmpty() && expTrends.isEmpty()) {
            return VisualizationResponse.empty("bar", "Revenue vs Expenses (₹)", "No billing or expense records available yet.");
        }

        // Map year-month to revenue and expense
        Map<String, BigDecimal> revMap = new LinkedHashMap<>();
        Map<String, BigDecimal> expMap = new LinkedHashMap<>();
        Set<String> periods = new TreeSet<>();

        for (Object[] r : revTrends) {
            String key = formatYearMonth(((Number) r[0]).intValue(), ((Number) r[1]).intValue());
            revMap.put(key, (BigDecimal) r[2]);
            periods.add(key);
        }
        for (Object[] e : expTrends) {
            String key = formatYearMonth(((Number) e[0]).intValue(), ((Number) e[1]).intValue());
            expMap.put(key, (BigDecimal) e[2]);
            periods.add(key);
        }

        List<String> labels = new ArrayList<>();
        List<BigDecimal> revData = new ArrayList<>();
        List<BigDecimal> expData = new ArrayList<>();

        for (String p : periods) {
            labels.add(p);
            revData.add(revMap.getOrDefault(p, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
            expData.add(expMap.getOrDefault(p, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
        }

        ChartDatasetDto revDataset = new ChartDatasetDto(
                "Revenue (₹)",
                revData,
                "rgba(46, 125, 50, 0.8)",
                "rgba(46, 125, 50, 1)"
        );

        ChartDatasetDto expDataset = new ChartDatasetDto(
                "Expenses (₹)",
                expData,
                "rgba(211, 47, 47, 0.8)",
                "rgba(211, 47, 47, 1)"
        );

        return new VisualizationResponse("bar", "Revenue vs Expenses (₹)", labels, List.of(revDataset, expDataset), true, null);
    }

    private VisualizationResponse buildMillDistributionChart(Long ownerId) {
        List<Object[]> rawMills = tripRepository.getMillWeightBreakdown(ownerId);
        if (rawMills.isEmpty()) {
            return VisualizationResponse.empty("bar", "Sugarcane Weight by Mill (Tons)", "No trip records available yet.");
        }

        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        for (Object[] row : rawMills) {
            labels.add((String) row[0]);
            data.add(((BigDecimal) row[1]).setScale(2, RoundingMode.HALF_UP));
        }

        ChartDatasetDto dataset = new ChartDatasetDto(
                "Tons Transported",
                data,
                "rgba(2, 119, 189, 0.8)",
                "rgba(2, 119, 189, 1)"
        );

        return new VisualizationResponse("bar", "Sugarcane Weight by Mill (Tons)", labels, List.of(dataset), true, null);
    }

    private VisualizationResponse buildLabourDistributionChart(Long ownerId) {
        List<Object[]> catCounts = labourRepository.getCategoryCountsByOwnerId(ownerId);
        if (catCounts.isEmpty()) {
            return VisualizationResponse.empty("doughnut", "Labour Category Distribution", "No labour records found.");
        }

        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();

        for (Object[] row : catCounts) {
            labels.add(row[0].toString().replace("_", " "));
            data.add(((Number) row[1]).longValue());
        }

        List<String> colors = List.of("#2e7d32", "#f57f17", "#0277bd", "#6a1b9a");

        ChartDatasetDto dataset = new ChartDatasetDto(
                "Workers",
                data,
                colors.subList(0, Math.min(colors.size(), labels.size())),
                "#ffffff"
        );

        return new VisualizationResponse("doughnut", "Labour Category Distribution", labels, List.of(dataset), true, null);
    }

    private VisualizationResponse buildProfitTrendChart(Long ownerId) {
        List<Object[]> revTrends = billingRepository.getMonthlyRevenueTrends(ownerId);
        List<Object[]> expTrends = expenseRepository.getMonthlyExpenseTrends(ownerId);

        if (revTrends.isEmpty() && expTrends.isEmpty()) {
            return VisualizationResponse.empty("line", "Monthly Profit Trend (₹)", "Insufficient billing and expense data for profit calculation.");
        }

        Map<String, BigDecimal> revMap = new LinkedHashMap<>();
        Map<String, BigDecimal> expMap = new LinkedHashMap<>();
        Set<String> periods = new TreeSet<>();

        for (Object[] r : revTrends) {
            String key = formatYearMonth(((Number) r[0]).intValue(), ((Number) r[1]).intValue());
            revMap.put(key, (BigDecimal) r[2]);
            periods.add(key);
        }
        for (Object[] e : expTrends) {
            String key = formatYearMonth(((Number) e[0]).intValue(), ((Number) e[1]).intValue());
            expMap.put(key, (BigDecimal) e[2]);
            periods.add(key);
        }

        List<String> labels = new ArrayList<>();
        List<BigDecimal> profitData = new ArrayList<>();

        for (String p : periods) {
            labels.add(p);
            BigDecimal rev = revMap.getOrDefault(p, BigDecimal.ZERO);
            BigDecimal exp = expMap.getOrDefault(p, BigDecimal.ZERO);
            profitData.add(rev.subtract(exp).setScale(2, RoundingMode.HALF_UP));
        }

        ChartDatasetDto dataset = new ChartDatasetDto(
                "Net Profit (₹)",
                profitData,
                "rgba(76, 175, 80, 0.2)",
                "rgba(76, 175, 80, 1)",
                true
        );

        return new VisualizationResponse("line", "Monthly Profit Trend (₹)", labels, List.of(dataset), true, null);
    }

    private LocalDate[] parseFilterRange(String filter) {
        if (filter == null || filter.equalsIgnoreCase("ALL")) {
            return new LocalDate[]{null, null};
        }
        LocalDate now = LocalDate.now();
        switch (filter.toUpperCase()) {
            case "TODAY":
                return new LocalDate[]{now, now};
            case "7D":
                return new LocalDate[]{now.minusDays(7), now};
            case "30D":
                return new LocalDate[]{now.minusDays(30), now};
            case "MONTH":
                return new LocalDate[]{now.withDayOfMonth(1), now};
            case "YEAR":
                return new LocalDate[]{now.withDayOfYear(1), now};
            default:
                return new LocalDate[]{null, null};
        }
    }

    private String getMonthName(int month) {
        return java.time.Month.of(month).name().substring(0, 3);
    }

    private String formatYearMonth(int year, int month) {
        return getMonthName(month) + " " + year;
    }
}
