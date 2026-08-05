package com.example.payment_processing.dto;

import com.example.payment_processing.model.PaymentStatus;

public class UpdatePaymentStatusRequest {

    private PaymentStatus status;


    public PaymentStatus getStatus() {
        return status;
    }


    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
}