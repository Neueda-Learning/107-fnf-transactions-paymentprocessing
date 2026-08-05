package com.example.payment_processing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_fx_details")
public class PaymentFxDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "payment_id", unique = true, nullable = false)
    private Payment payment;

    @Column(name = "sender_currency", nullable = false, length = 3)
    private String senderCurrency;

    @Column(name = "receiver_currency", nullable = false, length = 3)
    private String receiverCurrency;

    @Column(name = "exchange_rate", nullable = false, precision = 18, scale = 6)
    private BigDecimal exchangeRate;

    @Column(name = "fx_fee_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal fxFeeAmount;

    @Column(name = "converted_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal convertedAmount;

    @Column(name = "converted_at")
    private LocalDateTime convertedAt;

    public PaymentFxDetails() {}

    public Long getId() { return id; }

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }

    public String getSenderCurrency() { return senderCurrency; }
    public void setSenderCurrency(String senderCurrency) { this.senderCurrency = senderCurrency; }

    public String getReceiverCurrency() { return receiverCurrency; }
    public void setReceiverCurrency(String receiverCurrency) { this.receiverCurrency = receiverCurrency; }

    public BigDecimal getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(BigDecimal exchangeRate) { this.exchangeRate = exchangeRate; }

    public BigDecimal getFxFeeAmount() { return fxFeeAmount; }
    public void setFxFeeAmount(BigDecimal fxFeeAmount) { this.fxFeeAmount = fxFeeAmount; }

    public BigDecimal getConvertedAmount() { return convertedAmount; }
    public void setConvertedAmount(BigDecimal convertedAmount) { this.convertedAmount = convertedAmount; }

    public LocalDateTime getConvertedAt() { return convertedAt; }
    public void setConvertedAt(LocalDateTime convertedAt) { this.convertedAt = convertedAt; }
}

