package com.example.trackbill.AIBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AiChatResponse {
    private String reply;

    @JsonProperty("isGeminiConfigured")
    private boolean isGeminiConfigured;

    @JsonProperty("configured")
    private boolean configured;

    @JsonProperty("domainContextSummary")
    private String domainContextSummary;

    @JsonProperty("contextSummary")
    private String contextSummary;

    public AiChatResponse() {
    }

    public AiChatResponse(String reply, boolean isGeminiConfigured, String domainContextSummary) {
        this.reply = reply;
        this.isGeminiConfigured = isGeminiConfigured;
        this.configured = isGeminiConfigured;
        this.domainContextSummary = domainContextSummary;
        this.contextSummary = domainContextSummary;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public boolean isGeminiConfigured() {
        return isGeminiConfigured;
    }

    public void setGeminiConfigured(boolean geminiConfigured) {
        this.isGeminiConfigured = geminiConfigured;
        this.configured = geminiConfigured;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
        this.isGeminiConfigured = configured;
    }

    public String getDomainContextSummary() {
        return domainContextSummary;
    }

    public void setDomainContextSummary(String domainContextSummary) {
        this.domainContextSummary = domainContextSummary;
        this.contextSummary = domainContextSummary;
    }

    public String getContextSummary() {
        return contextSummary;
    }

    public void setContextSummary(String contextSummary) {
        this.contextSummary = contextSummary;
        this.domainContextSummary = contextSummary;
    }
}
