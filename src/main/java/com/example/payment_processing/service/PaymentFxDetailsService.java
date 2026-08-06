package com.example.payment_processing.service;

import com.example.payment_processing.exception.PaymentFxDetailsNotFoundException;
import com.example.payment_processing.model.PaymentFxDetails;
import com.example.payment_processing.repository.PaymentFxDetailsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentFxDetailsService {

    private final PaymentFxDetailsRepository paymentFxDetailsRepository;

    public PaymentFxDetailsService(PaymentFxDetailsRepository paymentFxDetailsRepository) {
        this.paymentFxDetailsRepository = paymentFxDetailsRepository;
    }

    public List<PaymentFxDetails> getAllPaymentFxDetails() {
        return paymentFxDetailsRepository.findAll();
    }

    public PaymentFxDetails getPaymentFxDetailsById(Long id) {
        return paymentFxDetailsRepository.findById(id)
                .orElseThrow(() -> new PaymentFxDetailsNotFoundException(
                        "Payment FX details with id " + id + " not found"
                ));
    }
}

