package com.example.trackbill.AIBackend.dto;

public class AiChatResponse {
    private String reply;
    private boolean isGeminiConfigured;
    private String domainContextSummary;

    public AiChatResponse() {
    }

    public AiChatResponse(String reply, boolean isGeminiConfigured, String domainContextSummary) {
        this.reply = reply;
        this.isGeminiConfigured = isGeminiConfigured;
        this.domainContextSummary = domainContextSummary;
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
        isGeminiConfigured = geminiConfigured;
    }

    public String getDomainContextSummary() {
        return domainContextSummary;
    }

    public void setDomainContextSummary(String domainContextSummary) {
        this.domainContextSummary = domainContextSummary;
    }
}
