package com.example.payment_processing.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal refundAmount;

    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private RefundStatus status;


    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;


    // Required by JPA
    public Refund() {
    }


    // Getters

    public Long getId() {
        return id;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public Payment getPayment() {
        return payment;
    }


    // Setters

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setStatus(RefundStatus status) {
        this.status = status;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }
}