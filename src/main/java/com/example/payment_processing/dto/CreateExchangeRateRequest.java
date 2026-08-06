package com.example.payment_processing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public class CreateExchangeRateRequest {

    @NotBlank
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency code must be exactly 3 letters")
    private String currencyCode;

    @NotNull
    @DecimalMin(value = "0.000001", message = "Rate must be greater than 0")
    private BigDecimal rateToUsd;

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getRateToUsd() {
        return rateToUsd;
    }

    public void setRateToUsd(BigDecimal rateToUsd) {
        this.rateToUsd = rateToUsd;
    }
}

