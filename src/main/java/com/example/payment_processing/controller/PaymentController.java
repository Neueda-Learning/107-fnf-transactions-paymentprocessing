package com.example.payment_processing.controller;

import com.example.payment_processing.dto.CreatePaymentRequest;
import com.example.payment_processing.dto.PaymentQuoteResponse;
import com.example.payment_processing.model.Payment;
import com.example.payment_processing.service.PaymentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.example.payment_processing.dto.UpdatePaymentStatusRequest;
import com.example.payment_processing.model.PaymentStatus;

import java.util.List;


@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;


    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Payment createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {

        return paymentService.createPayment(request);
    }

    @GetMapping("/quote")
    public PaymentQuoteResponse getPaymentQuote(
            @RequestParam Long invoiceId,
            @RequestParam String currency) {

        return paymentService.getPaymentQuote(invoiceId, currency);
    }


    @GetMapping
    public List<Payment> getAllPayments() {

        return paymentService.getAllPayments();
    }


    @GetMapping("/{id}")
    public Payment getPaymentById(
            @PathVariable Long id) {

        return paymentService.getPaymentById(id);
    }
    @PutMapping("/{id}/status")
    public Payment updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody UpdatePaymentStatusRequest request) {

        return paymentService.updatePaymentStatus(
                id,
                request.getStatus()
        );
    }

}