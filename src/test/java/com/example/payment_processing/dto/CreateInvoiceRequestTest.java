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

class CreateInvoiceRequestTest {

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
        CreateInvoiceRequest request = validRequest();

        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void invoiceNumber_blank_hasViolation() {
        CreateInvoiceRequest request = validRequest();
        request.setInvoiceNumber(" ");

        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("invoiceNumber", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void invoiceAmount_null_hasViolation() {
        CreateInvoiceRequest request = validRequest();
        request.setInvoiceAmount(null);

        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("invoiceAmount", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void invoiceAmount_belowMinimum_hasViolation() {
        CreateInvoiceRequest request = validRequest();
        request.setInvoiceAmount(new BigDecimal("0.00"));

        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("invoiceAmount", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void vendorId_null_hasViolation() {
        CreateInvoiceRequest request = validRequest();
        request.setVendorId(null);

        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("vendorId", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void currency_blank_hasViolation() {
        CreateInvoiceRequest request = validRequest();
        request.setCurrency(" ");

        Set<ConstraintViolation<CreateInvoiceRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("currency", violations.iterator().next().getPropertyPath().toString());
    }

    private CreateInvoiceRequest validRequest() {
        CreateInvoiceRequest request = new CreateInvoiceRequest();
        request.setInvoiceNumber("INV-1001");
        request.setInvoiceAmount(new BigDecimal("10.00"));
        request.setVendorId(1L);
        request.setCurrency("USD");
        return request;
    }
}

