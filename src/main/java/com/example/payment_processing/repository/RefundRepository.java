package com.example.payment_processing.repository;

import com.example.payment_processing.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    boolean existsByPayment_Id(Long paymentId);

}