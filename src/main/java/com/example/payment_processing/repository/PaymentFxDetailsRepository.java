package com.example.payment_processing.repository;

import com.example.payment_processing.model.PaymentFxDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentFxDetailsRepository extends JpaRepository<PaymentFxDetails, Long> {
}

