package com.example.payment_processing.service;

import com.example.payment_processing.exception.PaymentFxDetailsNotFoundException;
import com.example.payment_processing.model.PaymentFxDetails;
import com.example.payment_processing.repository.PaymentFxDetailsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentFxDetailsServiceTest {

    @Mock
    private PaymentFxDetailsRepository paymentFxDetailsRepository;

    @InjectMocks
    private PaymentFxDetailsService paymentFxDetailsService;

    @Test
    void getAllPaymentFxDetails_shouldReturnAllRows() {
        PaymentFxDetails one = new PaymentFxDetails();
        PaymentFxDetails two = new PaymentFxDetails();

        when(paymentFxDetailsRepository.findAll()).thenReturn(Arrays.asList(one, two));

        List<PaymentFxDetails> results = paymentFxDetailsService.getAllPaymentFxDetails();

        assertEquals(2, results.size());
    }

    @Test
    void getPaymentFxDetailsById_shouldReturnRowWhenFound() {
        PaymentFxDetails fxDetails = new PaymentFxDetails();

        when(paymentFxDetailsRepository.findById(10L)).thenReturn(Optional.of(fxDetails));

        PaymentFxDetails result = paymentFxDetailsService.getPaymentFxDetailsById(10L);

        assertEquals(fxDetails, result);
    }

    @Test
    void getPaymentFxDetailsById_shouldThrowWhenNotFound() {
        when(paymentFxDetailsRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PaymentFxDetailsNotFoundException.class,
                () -> paymentFxDetailsService.getPaymentFxDetailsById(999L));
    }
}

