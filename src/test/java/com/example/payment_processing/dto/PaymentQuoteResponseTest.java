package com.example.payment_processing.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentQuoteResponseTest {

    @Test
    void defaultConstructor_setsCrossCurrencyFalse() {
        PaymentQuoteResponse response = new PaymentQuoteResponse();

        assertFalse(response.isCrossCurrency());
    }

    @Test
    void gettersAndSetters_storeAndReturnAllValues() {
        PaymentQuoteResponse response = new PaymentQuoteResponse();

        response.setInvoiceId(1001L);
        response.setInvoiceCurrency("USD");
        response.setInvoiceAmount(new BigDecimal("250.75"));
        response.setPaymentCurrency("EUR");
        response.setRequiredPaymentAmount(new BigDecimal("215.50"));
        response.setCrossRate(new BigDecimal("1.163200"));
        response.setFxFeePercent(new BigDecimal("2.00"));
        response.setFxFeeAmount(new BigDecimal("5.01"));
        response.setConvertedAmount(new BigDecimal("250.75"));
        response.setCrossCurrency(true);

        assertEquals(1001L, response.getInvoiceId());
        assertEquals("USD", response.getInvoiceCurrency());
        assertEquals(new BigDecimal("250.75"), response.getInvoiceAmount());
        assertEquals("EUR", response.getPaymentCurrency());
        assertEquals(new BigDecimal("215.50"), response.getRequiredPaymentAmount());
        assertEquals(new BigDecimal("1.163200"), response.getCrossRate());
        assertEquals(new BigDecimal("2.00"), response.getFxFeePercent());
        assertEquals(new BigDecimal("5.01"), response.getFxFeeAmount());
        assertEquals(new BigDecimal("250.75"), response.getConvertedAmount());
        assertTrue(response.isCrossCurrency());
    }
}

