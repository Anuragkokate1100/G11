package com.example.trackbill.AIBackend.controller;

import com.example.trackbill.AIBackend.dto.ApiResponse;
import com.example.trackbill.AIBackend.dto.DashboardStatsResponse;
import com.example.trackbill.AIBackend.dto.VisualizationResponse;
import com.example.trackbill.AIBackend.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"", "/stats"})
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats(@RequestParam(defaultValue = "ALL") String filter) {
        DashboardStatsResponse stats = dashboardService.getDashboardStats(filter);
        return ResponseEntity.ok(ApiResponse.ok("Dashboard statistics retrieved", stats));
    }

    @GetMapping("/visualizations")
    public ResponseEntity<ApiResponse<Map<String, VisualizationResponse>>> getVisualizations(
            @RequestParam(defaultValue = "ALL") String filter) {
        Map<String, VisualizationResponse> visualizations = dashboardService.getDashboardVisualizations(filter);
        return ResponseEntity.ok(ApiResponse.ok("Dashboard visualizations retrieved", visualizations));
    }
}
