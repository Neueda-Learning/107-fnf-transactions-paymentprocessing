package com.example.payment_processing.exception;

public class DuplicateExchangeRateException extends RuntimeException {

    public DuplicateExchangeRateException(String message) {
        super(message);
    }
}

