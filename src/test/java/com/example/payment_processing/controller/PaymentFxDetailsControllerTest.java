package com.example.payment_processing.controller;

import com.example.payment_processing.model.PaymentFxDetails;
import com.example.payment_processing.service.PaymentFxDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentFxDetailsControllerTest {

    @Mock
    private PaymentFxDetailsService paymentFxDetailsService;

    @InjectMocks
    private PaymentFxDetailsController paymentFxDetailsController;

    @Test
    void getAllPaymentFxDetails_shouldDelegateToService() {
        List<PaymentFxDetails> expected = List.of(new PaymentFxDetails());
        when(paymentFxDetailsService.getAllPaymentFxDetails()).thenReturn(expected);

        List<PaymentFxDetails> result = paymentFxDetailsController.getAllPaymentFxDetails();

        assertEquals(expected, result);
        verify(paymentFxDetailsService).getAllPaymentFxDetails();
    }

    @Test
    void getPaymentFxDetailsById_shouldDelegateToService() {
        PaymentFxDetails expected = new PaymentFxDetails();
        when(paymentFxDetailsService.getPaymentFxDetailsById(1L)).thenReturn(expected);

        PaymentFxDetails result = paymentFxDetailsController.getPaymentFxDetailsById(1L);

        assertEquals(expected, result);
        verify(paymentFxDetailsService).getPaymentFxDetailsById(1L);
    }
}
