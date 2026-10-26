package com.example.trackbill.AIBackend.dto;

import java.util.List;

public class VisualizationResponse {
    private String chartType;
    private String title;
    private List<String> labels;
    private List<ChartDatasetDto> datasets;
    private boolean hasData;
    private String emptyMessage;

    public VisualizationResponse() {
        this.hasData = false;
        this.emptyMessage = "No visualization data available for the selected period.";
    }

    public VisualizationResponse(String chartType, String title, List<String> labels, List<ChartDatasetDto> datasets, boolean hasData, String emptyMessage) {
        this.chartType = chartType;
        this.title = title;
        this.labels = labels;
        this.datasets = datasets;
        this.hasData = hasData;
        this.emptyMessage = emptyMessage;
    }

    public static VisualizationResponse empty(String chartType, String title, String emptyMessage) {
        VisualizationResponse res = new VisualizationResponse();
        res.setChartType(chartType);
        res.setTitle(title);
        res.setHasData(false);
        res.setEmptyMessage(emptyMessage != null ? emptyMessage : "No visualization data available for the selected period.");
        return res;
    }

    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<ChartDatasetDto> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<ChartDatasetDto> datasets) {
        this.datasets = datasets;
    }

    public boolean isHasData() {
        return hasData;
    }

    public void setHasData(boolean hasData) {
        this.hasData = hasData;
    }

    public String getEmptyMessage() {
        return emptyMessage;
    }

    public void setEmptyMessage(String emptyMessage) {
        this.emptyMessage = emptyMessage;
    }
}
