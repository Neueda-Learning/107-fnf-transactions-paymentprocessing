package com.example.payment_processing.controller;

import com.example.payment_processing.dto.CreateRefundRequest;
import com.example.payment_processing.model.Refund;
import com.example.payment_processing.service.RefundService;
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
}
