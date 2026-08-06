package com.example.payment_processing.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateRefundRequestTest {

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
        CreateRefundRequest request = new CreateRefundRequest();
        request.setPaymentId(1L);

        Set<ConstraintViolation<CreateRefundRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void paymentId_null_hasViolation() {
        CreateRefundRequest request = new CreateRefundRequest();
        request.setPaymentId(null);

        Set<ConstraintViolation<CreateRefundRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        ConstraintViolation<CreateRefundRequest> violation = violations.iterator().next();
        assertEquals("paymentId", violation.getPropertyPath().toString());
        assertEquals("Payment ID is required", violation.getMessage());
    }
}

