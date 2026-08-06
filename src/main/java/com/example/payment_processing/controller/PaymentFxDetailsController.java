package com.example.payment_processing.controller;

import com.example.payment_processing.model.PaymentFxDetails;
import com.example.payment_processing.service.PaymentFxDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/payment-fx-details")
public class PaymentFxDetailsController {

    private final PaymentFxDetailsService paymentFxDetailsService;

    public PaymentFxDetailsController(PaymentFxDetailsService paymentFxDetailsService) {
        this.paymentFxDetailsService = paymentFxDetailsService;
    }

    @GetMapping
    public List<PaymentFxDetails> getAllPaymentFxDetails() {
        return paymentFxDetailsService.getAllPaymentFxDetails();
    }

    @GetMapping("/{id}")
    public PaymentFxDetails getPaymentFxDetailsById(@PathVariable Long id) {
        return paymentFxDetailsService.getPaymentFxDetailsById(id);
    }
}

