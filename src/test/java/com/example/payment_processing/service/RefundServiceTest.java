package com.example.payment_processing.service;

import com.example.payment_processing.dto.CreateRefundRequest;
import com.example.payment_processing.exception.PaymentNotFoundException;
import com.example.payment_processing.model.Invoice;
import com.example.payment_processing.model.Payment;
import com.example.payment_processing.model.PaymentStatus;
import com.example.payment_processing.model.Refund;
import com.example.payment_processing.model.RefundStatus;
import com.example.payment_processing.repository.PaymentRepository;
import com.example.payment_processing.repository.RefundRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private RefundService refundService;

    @Test
    void createRefund_success_whenPaymentIsCompletedAndOverpaid() {
        Payment payment = payment("120.00", PaymentStatus.COMPLETED);
        CreateRefundRequest request = refundRequest(1L);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(refundRepository.existsByPayment_Id(null)).thenReturn(false);
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Refund saved = refundService.createRefund(request);

        assertEquals(new BigDecimal("20.00"), saved.getRefundAmount());
        assertEquals(RefundStatus.CREATED, saved.getStatus());
        assertEquals(payment, saved.getPayment());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void createRefund_throwsWhenPaymentNotFound() {
        CreateRefundRequest request = refundRequest(99L);
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        PaymentNotFoundException ex = assertThrows(PaymentNotFoundException.class,
                () -> refundService.createRefund(request));

        assertEquals("Payment with id 99 not found", ex.getMessage());
    }

    @Test
    void createRefund_throwsWhenPaymentIsNotCompleted() {
        Payment payment = payment("120.00", PaymentStatus.SENT);
        CreateRefundRequest request = refundRequest(2L);

        when(paymentRepository.findById(2L)).thenReturn(Optional.of(payment));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> refundService.createRefund(request));

        assertEquals("Only completed payments can be refunded", ex.getMessage());
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void createRefund_throwsWhenRefundAlreadyExists() {
        Payment payment = payment("120.00", PaymentStatus.COMPLETED);
        CreateRefundRequest request = refundRequest(3L);

        when(paymentRepository.findById(3L)).thenReturn(Optional.of(payment));
        when(refundRepository.existsByPayment_Id(null)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> refundService.createRefund(request));

        assertEquals("Refund already exists for this payment", ex.getMessage());
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void createRefund_throwsWhenNoOverpaymentExists() {
        Payment payment = payment("100.00", PaymentStatus.COMPLETED);
        CreateRefundRequest request = refundRequest(4L);

        when(paymentRepository.findById(4L)).thenReturn(Optional.of(payment));
        when(refundRepository.existsByPayment_Id(null)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> refundService.createRefund(request));

        assertEquals("No refund required. Payment amount matches invoice amount", ex.getMessage());
        verify(refundRepository, never()).save(any(Refund.class));
    }

    @Test
    void getAllRefunds_returnsRepositoryValues() {
        Refund first = new Refund();
        Refund second = new Refund();
        when(refundRepository.findAll()).thenReturn(List.of(first, second));

        List<Refund> refunds = refundService.getAllRefunds();

        assertEquals(List.of(first, second), refunds);
    }

    @Test
    void getRefundById_returnsRefundWhenFound() {
        Refund refund = new Refund();
        when(refundRepository.findById(7L)).thenReturn(Optional.of(refund));

        Refund found = refundService.getRefundById(7L);

        assertEquals(refund, found);
    }

    @Test
    void getRefundById_throwsWhenMissing() {
        when(refundRepository.findById(8L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> refundService.getRefundById(8L));

        assertEquals("Refund with id 8 not found", ex.getMessage());
    }

    private CreateRefundRequest refundRequest(Long paymentId) {
        CreateRefundRequest request = new CreateRefundRequest();
        request.setPaymentId(paymentId);
        return request;
    }

    private Payment payment(String paymentAmount, PaymentStatus status) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceAmount(new BigDecimal("100.00"));

        Payment payment = new Payment();
        payment.setAmount(new BigDecimal(paymentAmount));
        payment.setStatus(status);
        payment.setInvoice(invoice);
        return payment;
    }
}
