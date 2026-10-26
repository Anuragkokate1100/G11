package com.example.trackbill.AIBackend.dto;

import java.util.List;

public class ChartDatasetDto {
    private String label;
    private List<?> data;
    private Object backgroundColor;
    private Object borderColor;
    private Boolean fill;

    public ChartDatasetDto() {
    }

    public ChartDatasetDto(String label, List<?> data, Object backgroundColor, Object borderColor) {
        this.label = label;
        this.data = data;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
    }

    public ChartDatasetDto(String label, List<?> data, Object backgroundColor, Object borderColor, Boolean fill) {
        this.label = label;
        this.data = data;
        this.backgroundColor = backgroundColor;
        this.borderColor = borderColor;
        this.fill = fill;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<?> getData() {
        return data;
    }

    public void setData(List<?> data) {
        this.data = data;
    }

    public Object getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Object backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Object getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Object borderColor) {
        this.borderColor = borderColor;
    }

    public Boolean getFill() {
        return fill;
    }

    public void setFill(Boolean fill) {
        this.fill = fill;
    }
}
