package com.example.payment_processing.controller;

import com.example.payment_processing.dto.CreatePaymentRequest;
import com.example.payment_processing.dto.PaymentQuoteResponse;
import com.example.payment_processing.dto.UpdatePaymentStatusRequest;
import com.example.payment_processing.model.Payment;
import com.example.payment_processing.model.PaymentStatus;
import com.example.payment_processing.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void createPayment_shouldDelegateToService() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setInvoiceId(1L);
        request.setAmount(new BigDecimal("150.00"));
        request.setCurrency("USD");
        request.setSenderAccount("S-1");
        request.setReceiverAccount("R-1");

        Payment expected = new Payment();
        expected.setCurrency("USD");

        when(paymentService.createPayment(request)).thenReturn(expected);

        Payment result = paymentController.createPayment(request);

        assertEquals(expected, result);
        verify(paymentService).createPayment(request);
    }

    @Test
    void getPaymentQuote_shouldDelegateToService() {
        PaymentQuoteResponse expected = new PaymentQuoteResponse();
        expected.setInvoiceId(1L);

        when(paymentService.getPaymentQuote(1L, "EUR")).thenReturn(expected);

        PaymentQuoteResponse result = paymentController.getPaymentQuote(1L, "EUR");

        assertEquals(expected, result);
        verify(paymentService).getPaymentQuote(1L, "EUR");
    }

    @Test
    void getAllPayments_shouldDelegateToService() {
        List<Payment> expected = List.of(new Payment());
        when(paymentService.getAllPayments()).thenReturn(expected);

        List<Payment> result = paymentController.getAllPayments();

        assertEquals(expected, result);
        verify(paymentService).getAllPayments();
    }

    @Test
    void getPaymentById_shouldDelegateToService() {
        Payment expected = new Payment();
        when(paymentService.getPaymentById(22L)).thenReturn(expected);

        Payment result = paymentController.getPaymentById(22L);

        assertEquals(expected, result);
        verify(paymentService).getPaymentById(22L);
    }

    @Test
    void updatePaymentStatus_shouldDelegateToService() {
        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();
        request.setStatus(PaymentStatus.VALIDATED);

        Payment expected = new Payment();
        expected.setStatus(PaymentStatus.VALIDATED);

        when(paymentService.updatePaymentStatus(7L, PaymentStatus.VALIDATED)).thenReturn(expected);

        Payment result = paymentController.updatePaymentStatus(7L, request);

        assertEquals(expected, result);
        verify(paymentService).updatePaymentStatus(7L, PaymentStatus.VALIDATED);
    }
}
