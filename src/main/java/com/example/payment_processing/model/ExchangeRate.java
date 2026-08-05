package com.example.payment_processing.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rates")
public class ExchangeRate {

    @Id
    @Column(name = "currency_code", length = 3)
    private String currencyCode;

    @Column(name = "rate_to_usd", nullable = false, precision = 18, scale = 6)
    private BigDecimal rateToUsd;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ExchangeRate() {}

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public BigDecimal getRateToUsd() { return rateToUsd; }
    public void setRateToUsd(BigDecimal rateToUsd) { this.rateToUsd = rateToUsd; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

