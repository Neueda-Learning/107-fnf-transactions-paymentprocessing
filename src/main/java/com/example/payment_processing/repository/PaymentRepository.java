package com.example.payment_processing.repository;

import com.example.payment_processing.model.Payment;
import com.example.payment_processing.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByInvoice_IdAndStatus(
            Long invoiceId,
            PaymentStatus status
    );

}