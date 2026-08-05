package com.example.payment_processing.dto;

import java.math.BigDecimal;

public class PaymentQuoteResponse {

    private Long invoiceId;
    private String invoiceCurrency;
    private BigDecimal invoiceAmount;

    private String paymentCurrency;

    // Amount the sender must send in paymentCurrency
    private BigDecimal requiredPaymentAmount;

    // null for same-currency quotes
    private BigDecimal crossRate;
    private BigDecimal fxFeePercent;
    private BigDecimal fxFeeAmount;

    // Amount that arrives in invoiceCurrency after fee (should equal invoiceAmount)
    private BigDecimal convertedAmount;

    private boolean isCrossCurrency;

    public PaymentQuoteResponse() {}

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public String getInvoiceCurrency() { return invoiceCurrency; }
    public void setInvoiceCurrency(String invoiceCurrency) { this.invoiceCurrency = invoiceCurrency; }

    public BigDecimal getInvoiceAmount() { return invoiceAmount; }
    public void setInvoiceAmount(BigDecimal invoiceAmount) { this.invoiceAmount = invoiceAmount; }

    public String getPaymentCurrency() { return paymentCurrency; }
    public void setPaymentCurrency(String paymentCurrency) { this.paymentCurrency = paymentCurrency; }

    public BigDecimal getRequiredPaymentAmount() { return requiredPaymentAmount; }
    public void setRequiredPaymentAmount(BigDecimal requiredPaymentAmount) { this.requiredPaymentAmount = requiredPaymentAmount; }

    public BigDecimal getCrossRate() { return crossRate; }
    public void setCrossRate(BigDecimal crossRate) { this.crossRate = crossRate; }

    public BigDecimal getFxFeePercent() { return fxFeePercent; }
    public void setFxFeePercent(BigDecimal fxFeePercent) { this.fxFeePercent = fxFeePercent; }

    public BigDecimal getFxFeeAmount() { return fxFeeAmount; }
    public void setFxFeeAmount(BigDecimal fxFeeAmount) { this.fxFeeAmount = fxFeeAmount; }

    public BigDecimal getConvertedAmount() { return convertedAmount; }
    public void setConvertedAmount(BigDecimal convertedAmount) { this.convertedAmount = convertedAmount; }

    public boolean isCrossCurrency() { return isCrossCurrency; }
    public void setCrossCurrency(boolean crossCurrency) { isCrossCurrency = crossCurrency; }
}

