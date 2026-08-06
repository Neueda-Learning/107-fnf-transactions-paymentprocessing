package com.example.payment_processing.exception;

import com.example.payment_processing.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    // ── PaymentNotFoundException ──────────────────────────────────────────────

    @Test
    void handlePaymentNotFound_shouldReturn404WithCorrectCode() {
        PaymentNotFoundException ex = new PaymentNotFoundException("Payment with id 1 not found");

        ResponseEntity<ErrorResponse> response = handler.handlePaymentNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PAYMENT_NOT_FOUND", response.getBody().getCode());
        assertEquals("Payment with id 1 not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
    }

    // ── DuplicatePaymentException ─────────────────────────────────────────────

    @Test
    void handleDuplicatePayment_shouldReturn409WithCorrectCode() {
        DuplicatePaymentException ex = new DuplicatePaymentException("Invoice already has a successful payment");

        ResponseEntity<ErrorResponse> response = handler.handleDuplicatePayment(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DUPLICATE_PAYMENT", response.getBody().getCode());
        assertEquals("Invoice already has a successful payment", response.getBody().getMessage());
        assertEquals(409, response.getBody().getStatus());
    }

    // ── InvalidStatusTransitionException ─────────────────────────────────────

    @Test
    void handleInvalidStatusTransition_shouldReturn400WithCorrectCode() {
        InvalidStatusTransitionException ex = new InvalidStatusTransitionException("Cannot move from CREATED to SENT");

        ResponseEntity<ErrorResponse> response = handler.handleInvalidStatusTransition(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_STATUS_TRANSITION", response.getBody().getCode());
        assertEquals("Cannot move from CREATED to SENT", response.getBody().getMessage());
        assertEquals(400, response.getBody().getStatus());
    }

    // ── ExchangeRateNotFoundException ─────────────────────────────────────────

    @Test
    void handleExchangeRateNotFound_shouldReturn404WithCorrectCode() {
        ExchangeRateNotFoundException ex = new ExchangeRateNotFoundException("Exchange rate for currency XYZ not found");

        ResponseEntity<ErrorResponse> response = handler.handleExchangeRateNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("EXCHANGE_RATE_NOT_FOUND", response.getBody().getCode());
        assertEquals("Exchange rate for currency XYZ not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
    }

    // ── DuplicateExchangeRateException ────────────────────────────────────────

    @Test
    void handleDuplicateExchangeRate_shouldReturn409WithCorrectCode() {
        DuplicateExchangeRateException ex = new DuplicateExchangeRateException("Exchange rate for currency USD already exists");

        ResponseEntity<ErrorResponse> response = handler.handleDuplicateExchangeRate(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DUPLICATE_EXCHANGE_RATE", response.getBody().getCode());
        assertEquals("Exchange rate for currency USD already exists", response.getBody().getMessage());
        assertEquals(409, response.getBody().getStatus());
    }

    // ── MethodArgumentNotValidException ──────────────────────────────────────

    @Test
    void handleValidationException_shouldReturn400WithJoinedFieldMessages() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("obj", "amount", "must not be null");
        FieldError fieldError2 = new FieldError("obj", "currency", "must not be blank");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDATION_ERROR", response.getBody().getCode());
        assertEquals("must not be null, must not be blank", response.getBody().getMessage());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void handleValidationException_whenSingleField_shouldReturnSingleMessage() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("obj", "invoiceId", "must not be null");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("must not be null", response.getBody().getMessage());
    }

    // ── ResponseStatusException ───────────────────────────────────────────────

    @Test
    void handleResponseStatusException_shouldReturnDynamicStatusWithCorrectCode() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found");

        ResponseEntity<ErrorResponse> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("REQUEST_ERROR", response.getBody().getCode());
        assertEquals("Invoice not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void handleResponseStatusException_withBadRequest_shouldReturn400() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than 0");

        ResponseEntity<ErrorResponse> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("REQUEST_ERROR", response.getBody().getCode());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void handleResponseStatusException_withConflict_shouldReturn409() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.CONFLICT, "Duplicate entry");

        ResponseEntity<ErrorResponse> response = handler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
    }

    // ── PaymentFxDetailsNotFoundException ────────────────────────────────────

    @Test
    void handlePaymentFxDetailsNotFound_shouldReturn404WithCorrectCode() {
        PaymentFxDetailsNotFoundException ex = new PaymentFxDetailsNotFoundException("Payment FX details with id 99 not found");

        ResponseEntity<ErrorResponse> response = handler.handlePaymentFxDetailsNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PAYMENT_FX_DETAILS_NOT_FOUND", response.getBody().getCode());
        assertEquals("Payment FX details with id 99 not found", response.getBody().getMessage());
        assertEquals(404, response.getBody().getStatus());
    }

    // ── Generic Exception ─────────────────────────────────────────────────────

    @Test
    void handleGeneralException_shouldReturn500WithCorrectCode() {
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGeneralException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PROCESSING_ERROR", response.getBody().getCode());
        assertEquals("Unexpected error", response.getBody().getMessage());
        assertEquals(500, response.getBody().getStatus());
    }

    @Test
    void handleGeneralException_shouldIncludeTimestamp() {
        Exception ex = new Exception("Some error");

        ResponseEntity<ErrorResponse> response = handler.handleGeneralException(ex);

        assertNotNull(response.getBody().getTimestamp());
    }
}

