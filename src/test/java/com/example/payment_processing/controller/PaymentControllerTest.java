package com.example.payment_processing.controller;

import com.example.payment_processing.dto.CreatePaymentRequest;
import com.example.payment_processing.dto.PaymentQuoteResponse;
import com.example.payment_processing.dto.UpdatePaymentStatusRequest;
import com.example.payment_processing.exception.DuplicatePaymentException;
import com.example.payment_processing.exception.InvalidStatusTransitionException;
import com.example.payment_processing.exception.PaymentNotFoundException;
import com.example.payment_processing.model.Payment;
import com.example.payment_processing.model.PaymentStatus;
import com.example.payment_processing.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

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
    void createPayment_whenInvoiceAlreadyPaid_shouldThrowDuplicatePaymentException() {
        when(paymentService.createPayment(any()))
                .thenThrow(new DuplicatePaymentException("Invoice already has a successful payment"));

        assertThrows(DuplicatePaymentException.class,
                () -> paymentController.createPayment(new CreatePaymentRequest()));
    }

    @Test
    void createPayment_whenInvoiceNotFound_shouldThrowPaymentNotFoundException() {
        when(paymentService.createPayment(any()))
                .thenThrow(new PaymentNotFoundException("Invoice not found"));

        assertThrows(PaymentNotFoundException.class,
                () -> paymentController.createPayment(new CreatePaymentRequest()));
    }

    @Test
    void createPayment_whenAmountInvalid_shouldThrowResponseStatusException() {
        when(paymentService.createPayment(any()))
                .thenThrow(new ResponseStatusException(BAD_REQUEST, "Amount must be greater than 0"));

        assertThrows(ResponseStatusException.class,
                () -> paymentController.createPayment(new CreatePaymentRequest()));
    }

    @Test
    void createPayment_whenAmountExceedsMax_shouldThrowResponseStatusException() {
        when(paymentService.createPayment(any()))
                .thenThrow(new ResponseStatusException(BAD_REQUEST, "Amount must not exceed 1,000,000"));

        assertThrows(ResponseStatusException.class,
                () -> paymentController.createPayment(new CreatePaymentRequest()));
    }

    @Test
    void createPayment_whenAmountHasTooManyDecimals_shouldThrowResponseStatusException() {
        when(paymentService.createPayment(any()))
                .thenThrow(new ResponseStatusException(BAD_REQUEST, "Amount must have maximum 2 decimal places"));

        assertThrows(ResponseStatusException.class,
                () -> paymentController.createPayment(new CreatePaymentRequest()));
    }

    @Test
    void createPayment_whenSenderEqualsReceiver_shouldThrowResponseStatusException() {
        when(paymentService.createPayment(any()))
                .thenThrow(new ResponseStatusException(BAD_REQUEST, "Sender account and receiver account must be different"));

        assertThrows(ResponseStatusException.class,
                () -> paymentController.createPayment(new CreatePaymentRequest()));
    }

    @Test
    void createPayment_whenAmountMismatchSameCurrency_shouldThrowResponseStatusException() {
        when(paymentService.createPayment(any()))
                .thenThrow(new ResponseStatusException(BAD_REQUEST, "Payment amount does not match invoice amount of 100.00"));

        assertThrows(ResponseStatusException.class,
                () -> paymentController.createPayment(new CreatePaymentRequest()));
    }

    @Test
    void createPayment_whenCrossCurrencyOutsideTolerance_shouldThrowResponseStatusException() {
        when(paymentService.createPayment(any()))
                .thenThrow(new ResponseStatusException(BAD_REQUEST, "Converted payment amount is outside allowed tolerance"));

        assertThrows(ResponseStatusException.class,
                () -> paymentController.createPayment(new CreatePaymentRequest()));
    }

    @Test
    void createPayment_whenVendorNotFound_shouldThrowResponseStatusException() {
        when(paymentService.createPayment(any()))
                .thenThrow(new ResponseStatusException(NOT_FOUND, "Vendor not found"));

        assertThrows(ResponseStatusException.class,
                () -> paymentController.createPayment(new CreatePaymentRequest()));
    }

    @Test
    void createPayment_whenReceiverAccountMismatch_shouldThrowResponseStatusException() {
        when(paymentService.createPayment(any()))
                .thenThrow(new ResponseStatusException(BAD_REQUEST, "Receiver account does not match vendor bank account"));

        assertThrows(ResponseStatusException.class,
                () -> paymentController.createPayment(new CreatePaymentRequest()));
    }

    @Test
    void createPayment_whenExchangeRateNotFound_shouldThrowResponseStatusException() {
        when(paymentService.createPayment(any()))
                .thenThrow(new ResponseStatusException(BAD_REQUEST, "Exchange rate not found for currency: EUR"));

        assertThrows(ResponseStatusException.class,
                () -> paymentController.createPayment(new CreatePaymentRequest()));
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
    void getPaymentQuote_whenExchangeRateNotFound_shouldThrowResponseStatusException() {
        when(paymentService.getPaymentQuote(1L, "EUR"))
                .thenThrow(new ResponseStatusException(BAD_REQUEST, "Exchange rate not found for currency: EUR"));

        assertThrows(ResponseStatusException.class,
                () -> paymentController.getPaymentQuote(1L, "EUR"));
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
    void getPaymentById_whenNotFound_shouldThrowPaymentNotFoundException() {
        when(paymentService.getPaymentById(999L))
                .thenThrow(new PaymentNotFoundException("Payment with id 999 not found"));

        assertThrows(PaymentNotFoundException.class,
                () -> paymentController.getPaymentById(999L));
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

    @Test
    void updatePaymentStatus_whenInvalidTransition_shouldThrowInvalidStatusTransitionException() {
        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();
        request.setStatus(PaymentStatus.SENT);

        when(paymentService.updatePaymentStatus(1L, PaymentStatus.SENT))
                .thenThrow(new InvalidStatusTransitionException("Cannot move payment status from CREATED to SENT"));

        assertThrows(InvalidStatusTransitionException.class,
                () -> paymentController.updatePaymentStatus(1L, request));
    }

    @Test
    void updatePaymentStatus_whenPaymentNotFound_shouldThrowPaymentNotFoundException() {
        UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();
        request.setStatus(PaymentStatus.VALIDATED);

        when(paymentService.updatePaymentStatus(999L, PaymentStatus.VALIDATED))
                .thenThrow(new PaymentNotFoundException("Payment with id 999 not found"));

        assertThrows(PaymentNotFoundException.class,
                () -> paymentController.updatePaymentStatus(999L, request));
    }
}
