package com.example.payment_processing.controller;

import com.example.payment_processing.dto.CreateRefundRequest;
import com.example.payment_processing.exception.PaymentNotFoundException;
import com.example.payment_processing.model.Refund;
import com.example.payment_processing.service.RefundService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundControllerTest {

    @Mock
    private RefundService refundService;

    @InjectMocks
    private RefundController refundController;

    @Test
    void createRefund_shouldDelegateToService() {
        CreateRefundRequest request = new CreateRefundRequest();
        request.setPaymentId(10L);

        Refund expected = new Refund();

        when(refundService.createRefund(request)).thenReturn(expected);

        Refund result = refundController.createRefund(request);

        assertEquals(expected, result);
        verify(refundService).createRefund(request);
    }

    @Test
    void createRefund_whenPaymentNotFound_shouldThrowPaymentNotFoundException() {
        when(refundService.createRefund(any()))
                .thenThrow(new PaymentNotFoundException("Payment with id 99 not found"));

        assertThrows(PaymentNotFoundException.class,
                () -> refundController.createRefund(new CreateRefundRequest()));
    }

    @Test
    void createRefund_whenPaymentNotCompleted_shouldThrowRuntimeException() {
        when(refundService.createRefund(any()))
                .thenThrow(new RuntimeException("Only completed payments can be refunded"));

        assertThrows(RuntimeException.class,
                () -> refundController.createRefund(new CreateRefundRequest()));
    }

    @Test
    void createRefund_whenAlreadyRefunded_shouldThrowRuntimeException() {
        when(refundService.createRefund(any()))
                .thenThrow(new RuntimeException("Refund already exists for this payment"));

        assertThrows(RuntimeException.class,
                () -> refundController.createRefund(new CreateRefundRequest()));
    }

    @Test
    void createRefund_whenNoRefundRequired_shouldThrowRuntimeException() {
        when(refundService.createRefund(any()))
                .thenThrow(new RuntimeException("No refund required. Payment amount matches invoice amount"));

        assertThrows(RuntimeException.class,
                () -> refundController.createRefund(new CreateRefundRequest()));
    }

    @Test
    void getAllRefunds_shouldDelegateToService() {
        List<Refund> expected = List.of(new Refund());
        when(refundService.getAllRefunds()).thenReturn(expected);

        List<Refund> result = refundController.getAllRefunds();

        assertEquals(expected, result);
        verify(refundService).getAllRefunds();
    }

    @Test
    void getRefundById_shouldDelegateToService() {
        Refund expected = new Refund();
        when(refundService.getRefundById(5L)).thenReturn(expected);

        Refund result = refundController.getRefundById(5L);

        assertEquals(expected, result);
        verify(refundService).getRefundById(5L);
    }

    @Test
    void getRefundById_whenNotFound_shouldThrowRuntimeException() {
        when(refundService.getRefundById(999L))
                .thenThrow(new RuntimeException("Refund with id 999 not found"));

        assertThrows(RuntimeException.class,
                () -> refundController.getRefundById(999L));
    }
}
