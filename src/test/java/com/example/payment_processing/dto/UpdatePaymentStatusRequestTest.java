package com.example.payment_processing.dto;

import com.example.payment_processing.model.PaymentStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UpdatePaymentStatusRequestTest {

    @Test
    void defaultConstructor_statusIsNull() {
        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();

        assertNull(request.getStatus());
    }

    @Test
    void setStatus_getStatusReturnsAssignedValue() {
        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();

        request.setStatus(PaymentStatus.COMPLETED);

        assertEquals(PaymentStatus.COMPLETED, request.getStatus());
    }
}

