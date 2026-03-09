package com.login.firstproject;

public class GmailEmailResponse {
    private int statusCode;
    private String message;

    public GmailEmailResponse() {}

    public GmailEmailResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "GmailEmailResponse{" +
                "statusCode=" + statusCode +
                ", message='" + message + '\'' +
                '}';
    }
}
