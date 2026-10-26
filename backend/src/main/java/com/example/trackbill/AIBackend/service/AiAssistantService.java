package com.example.trackbill.AIBackend.service;

import com.example.trackbill.AIBackend.dto.AiChatRequest;
import com.example.trackbill.AIBackend.dto.AiChatResponse;
import com.example.trackbill.AIBackend.model.ExpenseType;
import com.example.trackbill.AIBackend.model.TractorOwner;
import com.example.trackbill.AIBackend.repository.*;
import com.example.trackbill.AIBackend.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiAssistantService {

    private final TripRepository tripRepository;
    private final ExpenseRepository expenseRepository;
    private final BillingRepository billingRepository;
    private final LabourRepository labourRepository;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String geminiApiUrl;

    public AiAssistantService(TripRepository tripRepository,
                              ExpenseRepository expenseRepository,
                              BillingRepository billingRepository,
                              LabourRepository labourRepository) {
        this.tripRepository = tripRepository;
        this.expenseRepository = expenseRepository;
        this.billingRepository = billingRepository;
        this.labourRepository = labourRepository;
    }

    public AiChatResponse chat(AiChatRequest request) {
        TractorOwner owner = SecurityUtil.getCurrentOwner();
        Long ownerId = owner.getId();

        // 1. Gather REAL business metrics for the authenticated tractor owner
        long totalTrips = tripRepository.countByTractorOwnerId(ownerId);
        BigDecimal totalWeight = tripRepository.sumWeightByOwnerId(ownerId).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalDiesel = tripRepository.sumDieselByOwnerId(ownerId).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalRevenue = billingRepository.sumRevenueByOwnerId(ownerId).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalExpenses = expenseRepository.sumAmountByOwnerId(ownerId).setScale(2, RoundingMode.HALF_UP);
        BigDecimal dieselExpense = expenseRepository.sumAmountByOwnerIdAndExpenseType(ownerId, ExpenseType.DIESEL).setScale(2, RoundingMode.HALF_UP);
        BigDecimal labourExpense = expenseRepository.sumAmountByOwnerIdAndExpenseType(ownerId, ExpenseType.LABOUR).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netProfit = totalRevenue.subtract(totalExpenses).setScale(2, RoundingMode.HALF_UP);
        long labourCount = labourRepository.countByTractorOwnerId(ownerId);

        // Current month metrics
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate today = LocalDate.now();
        long monthTrips = tripRepository.countByTractorOwnerIdAndTripDateBetween(ownerId, startOfMonth, today);
        BigDecimal monthWeight = tripRepository.sumWeightByOwnerIdAndTripDateBetween(ownerId, startOfMonth, today).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthExpenses = expenseRepository.sumAmountByOwnerIdAndExpenseDateBetween(ownerId, startOfMonth, today).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthDieselExpense = expenseRepository.sumAmountByOwnerIdAndExpenseTypeAndExpenseDateBetween(ownerId, ExpenseType.DIESEL, startOfMonth, today).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthRevenue = billingRepository.sumRevenueByOwnerIdAndBillDateBetween(ownerId, startOfMonth, today).setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthProfit = monthRevenue.subtract(monthExpenses).setScale(2, RoundingMode.HALF_UP);

        // Top mills
        List<Object[]> mills = tripRepository.getMillWeightBreakdown(ownerId);
        String topMill = (mills != null && !mills.isEmpty()) ? (String) mills.get(0)[0] : "None recorded yet";

        String contextSummary = String.format(
                "Owner: %s (Tractor: %s)\n" +
                "All-Time Metrics: Trips: %d, Weight: %s tons, Diesel Consumed: %s L, Diesel Expense: ₹%s, Labour Expense: ₹%s, Total Expenses: ₹%s, Revenue: ₹%s, Net Profit: ₹%s, Active Labourers: %d, Top Sugar Mill: %s.\n" +
                "Current Month Metrics: Trips: %d, Weight: %s tons, Diesel Expense: ₹%s, Total Expenses: ₹%s, Revenue: ₹%s, Profit: ₹%s.",
                owner.getName(), owner.getTractorNumber(),
                totalTrips, totalWeight, totalDiesel, dieselExpense, labourExpense, totalExpenses, totalRevenue, netProfit, labourCount, topMill,
                monthTrips, monthWeight, monthDieselExpense, monthExpenses, monthRevenue, monthProfit
        );

        // 2. Check if Gemini API key is configured
        boolean isKeyConfigured = geminiApiKey != null && !geminiApiKey.isBlank();

        if (isKeyConfigured) {
            try {
                String reply = callGeminiApi(request.getMessage(), contextSummary);
                return new AiChatResponse(reply, true, contextSummary);
            } catch (Exception e) {
                // If API call fails (network/invalid key), fallback gracefully
                String fallback = generateRuleBasedResponse(request.getMessage(), totalTrips, totalWeight, totalDiesel, dieselExpense, totalRevenue, totalExpenses, netProfit, monthTrips, monthWeight, monthExpenses, monthRevenue, monthProfit, topMill);
                return new AiChatResponse(fallback + "\n\n*(Note: Gemini cloud API call timed out or failed; answered using your verified TrackBill local data)*", false, contextSummary);
            }
        } else {
            // Not configured: Answer intelligently from real local TrackBill data and provide configuration note
            String reply = generateRuleBasedResponse(request.getMessage(), totalTrips, totalWeight, totalDiesel, dieselExpense, totalRevenue, totalExpenses, netProfit, monthTrips, monthWeight, monthExpenses, monthRevenue, monthProfit, topMill);
            String fullReply = reply + "\n\n---\n*💡 Tip: To enable full Gemini generative capabilities, add your Gemini API key in `application.properties` (`gemini.api.key=YOUR_KEY` or environment variable `GEMINI_API_KEY`).*";
            return new AiChatResponse(fullReply, false, contextSummary);
        }
    }

    private String generateRuleBasedResponse(String question, long totalTrips, BigDecimal totalWeight, BigDecimal totalDiesel,
                                            BigDecimal dieselExpense, BigDecimal totalRevenue, BigDecimal totalExpenses,
                                            BigDecimal netProfit, long monthTrips, BigDecimal monthWeight, BigDecimal monthExpenses,
                                            BigDecimal monthRevenue, BigDecimal monthProfit, String topMill) {
        String q = question.toLowerCase();

        if (q.contains("diesel")) {
            return String.format("🚜 **Diesel Summary**:\n• Current Month Diesel Expense: **₹%s**\n• All-Time Diesel Expense: **₹%s**\n• Total Diesel Consumed: **%s Liters** across %d trips.",
                    monthExpenses, dieselExpense, totalDiesel, totalTrips);
        } else if (q.contains("weight") || q.contains("tons")) {
            return String.format("⚖️ **Sugarcane Weight Summary**:\n• Current Month Weight: **%s Tons** (%d trips)\n• All-Time Transported Weight: **%s Tons** (%d trips).",
                    monthWeight, monthTrips, totalWeight, totalTrips);
        } else if (q.contains("revenue") || q.contains("earning") || q.contains("bill") || q.contains("billing")) {
            return String.format("💰 **Revenue Summary**:\n• Current Month Billed Revenue: **₹%s**\n• All-Time Total Revenue: **₹%s**.",
                    monthRevenue, totalRevenue);
        } else if (q.contains("expense")) {
            return String.format("📊 **Expenses Summary**:\n• Current Month Expenses: **₹%s**\n• All-Time Total Expenses: **₹%s** (Diesel: ₹%s).",
                    monthExpenses, totalExpenses, dieselExpense);
        } else if (q.contains("profit")) {
            return String.format("📈 **Profit Summary**:\n• Current Month Profit: **₹%s** (Revenue ₹%s - Expenses ₹%s)\n• All-Time Net Profit: **₹%s**.",
                    monthProfit, monthRevenue, monthExpenses, netProfit);
        } else if (q.contains("mill")) {
            return String.format("🏭 **Sugar Mill Insights**:\n• Your primary sugar mill destination is **%s**.", topMill);
        } else if (q.contains("trip")) {
            return String.format("🚛 **Trip Summary**:\n• Current Month Trips: **%d trips**\n• Total Recorded Trips: **%d trips** transporting %s tons.",
                    monthTrips, totalTrips, totalWeight);
        } else {
            return String.format("🌾 **TrackBill Business Overview**:\n• Total Trips: **%d**\n• Sugarcane Weight: **%s Tons**\n• Total Revenue: **₹%s**\n• Total Expenses: **₹%s**\n• Net Profit: **₹%s**\n• Top Mill: **%s**\n\nAsk me specific questions like: *\"How much diesel did I use?\"*, *\"What is my profit this month?\"*, or *\"Total weight transported?\"*",
                    totalTrips, totalWeight, totalRevenue, totalExpenses, netProfit, topMill);
        }
    }

    private String callGeminiApi(String userPrompt, String businessContext) {
        RestTemplate restTemplate = new RestTemplate();
        String url = geminiApiUrl + "?key=" + geminiApiKey;

        String systemPrompt = "You are TrackBill AI, an intelligent agricultural transport assistant specialized in tractor sugarcane billing, expenses, and logistics for tractor owners in Maharashtra, India. " +
                "Answer the owner's question accurately using ONLY the following verified database metrics. Do not fabricate numbers. Format cleanly with bullet points, emojis, and Indian Rupees (₹).\n\n" +
                "OWNER'S CURRENT VERIFIED BUSINESS DATA:\n" + businessContext;

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", systemPrompt + "\n\nUser Question: " + userPrompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map body = response.getBody();
            List candidates = (List) body.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map firstCandidate = (Map) candidates.get(0);
                Map contentMap = (Map) firstCandidate.get("content");
                List parts = (List) contentMap.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    Map firstPart = (Map) parts.get(0);
                    return (String) firstPart.get("text");
                }
            }
        }

        return "Could not retrieve response from Gemini API.";
    }
}
