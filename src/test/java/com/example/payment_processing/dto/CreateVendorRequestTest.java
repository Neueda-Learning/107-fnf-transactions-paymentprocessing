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

class CreateVendorRequestTest {

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
        CreateVendorRequest request = validRequest();

        Set<ConstraintViolation<CreateVendorRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void name_blank_hasViolation() {
        CreateVendorRequest request = validRequest();
        request.setName(" ");

        Set<ConstraintViolation<CreateVendorRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("name", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void email_blank_hasViolation() {
        CreateVendorRequest request = validRequest();
        request.setEmail(" ");

        Set<ConstraintViolation<CreateVendorRequest>> violations = validator.validate(request);

        assertEquals(2, violations.size());
        assertTrue(violations.stream()
                .allMatch(v -> "email".equals(v.getPropertyPath().toString())));
    }

    @Test
    void email_invalidFormat_hasViolation() {
        CreateVendorRequest request = validRequest();
        request.setEmail("invalid-email-format");

        Set<ConstraintViolation<CreateVendorRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("email", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void bankAccount_blank_hasViolation() {
        CreateVendorRequest request = validRequest();
        request.setBankAccount(" ");

        Set<ConstraintViolation<CreateVendorRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("bankAccount", violations.iterator().next().getPropertyPath().toString());
    }

    @Test
    void country_blank_hasViolation() {
        CreateVendorRequest request = validRequest();
        request.setCountry(" ");

        Set<ConstraintViolation<CreateVendorRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("country", violations.iterator().next().getPropertyPath().toString());
    }

    private CreateVendorRequest validRequest() {
        CreateVendorRequest request = new CreateVendorRequest();
        request.setName("ACME Ltd");
        request.setEmail("acme@vendor.com");
        request.setBankAccount("ACCT-001");
        request.setCountry("US");
        return request;
    }
}

