package com.example.payment_processing.dto;

import java.time.LocalDateTime;

public class ErrorResponse {

    private String code;
    private String message;
    private int status;
    private LocalDateTime timestamp;


    public ErrorResponse(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }


    public String getCode() {
        return code;
    }


    public String getMessage() {
        return message;
    }


    public int getStatus() {
        return status;
    }


    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}