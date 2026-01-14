package com.projects.Rate_Limiter.dto;

public class RegisterResponse {

    private boolean success;
    private String message;
    private String apiKey;
    private String clientId;

    public RegisterResponse() {
    }

    public RegisterResponse(boolean success, String message, String apiKey, String clientId) {
        this.success = success;
        this.message = message;
        this.apiKey = apiKey;
        this.clientId = clientId;
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

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
