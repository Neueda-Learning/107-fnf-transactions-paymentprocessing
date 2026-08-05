package com.example.payment_processing.dto;

import jakarta.validation.constraints.NotNull;

public class CreateRefundRequest {

    @NotNull(message = "Payment ID is required")
    private Long paymentId;


    public Long getPaymentId() {
        return paymentId;
    }


    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
}