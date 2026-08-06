package com.example.payment_processing.exception;

public class ExchangeRateNotFoundException extends RuntimeException {

    public ExchangeRateNotFoundException(String message) {
        super(message);
    }
}

