package com.example.trackbill.AIBackend.dto;

public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private OwnerProfileDto owner;

    public AuthResponse() {
    }

    public AuthResponse(boolean success, String message, String token, OwnerProfileDto owner) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.owner = owner;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public OwnerProfileDto getOwner() {
        return owner;
    }

    public void setOwner(OwnerProfileDto owner) {
        this.owner = owner;
    }
}
