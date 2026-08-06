package com.example.payment_processing.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreatePaymentRequestTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDownValidator() {
        validatorFactory.close();
    }

    @Test
    void validRequest_hasNoViolations() {
        CreatePaymentRequest request = validRequest();

        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void invoiceId_null_hasViolation() {
        CreatePaymentRequest request = validRequest();
        request.setInvoiceId(null);

        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("invoiceId", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void amount_null_hasViolation() {
        CreatePaymentRequest request = validRequest();
        request.setAmount(null);

        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("amount", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void amount_belowMinimum_hasViolation() {
        CreatePaymentRequest request = validRequest();
        request.setAmount(new BigDecimal("0.00"));

        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("amount", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void currency_blank_hasViolation() {
        CreatePaymentRequest request = validRequest();
        request.setCurrency(" ");

        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("currency", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void senderAccount_blank_hasViolation() {
        CreatePaymentRequest request = validRequest();
        request.setSenderAccount(" ");

        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("senderAccount", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void receiverAccount_blank_hasViolation() {
        CreatePaymentRequest request = validRequest();
        request.setReceiverAccount(" ");

        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("receiverAccount", violations.iterator().next().getPropertyPath().toString());
    }

    private CreatePaymentRequest validRequest() {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setInvoiceId(1L);
        request.setAmount(new BigDecimal("10.00"));
        request.setCurrency("USD");
        request.setSenderAccount("SENDER-ACC-1");
        request.setReceiverAccount("RECEIVER-ACC-1");
        return request;
    }
}

