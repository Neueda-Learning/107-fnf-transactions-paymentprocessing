package com.example.payment_processing.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ErrorResponseTest {

    @Test
    void constructor_setsFieldsAndGettersReturnValues() {
        ErrorResponse response = new ErrorResponse("PAYMENT_NOT_FOUND", "Payment does not exist", 404);

        assertEquals("PAYMENT_NOT_FOUND", response.getCode());
        assertEquals("Payment does not exist", response.getMessage());
        assertEquals(404, response.getStatus());
    }

    @Test
    void constructor_setsTimestampAtCreationTime() {
        LocalDateTime before = LocalDateTime.now();
        ErrorResponse response = new ErrorResponse("ERR", "Something failed", 500);
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(response.getTimestamp());
        assertFalse(response.getTimestamp().isBefore(before));
        assertFalse(response.getTimestamp().isAfter(after));
    }
}

