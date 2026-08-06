package com.example.payment_processing.exception;

import com.example.payment_processing.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(
            PaymentNotFoundException exception) {

        ErrorResponse error = new ErrorResponse(
                "PAYMENT_NOT_FOUND",
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );

        return new ResponseEntity<>(
                error,
                HttpStatus.NOT_FOUND
        );
    }


    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePayment(
            DuplicatePaymentException exception) {

        ErrorResponse error = new ErrorResponse(
                "DUPLICATE_PAYMENT",
                exception.getMessage(),
                HttpStatus.CONFLICT.value()
        );

        return new ResponseEntity<>(
                error,
                HttpStatus.CONFLICT
        );
    }


    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatusTransition(
            InvalidStatusTransitionException exception) {

        ErrorResponse error = new ErrorResponse(
                "INVALID_STATUS_TRANSITION",
                exception.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(
                error,
                HttpStatus.BAD_REQUEST
        );
    }


    @ExceptionHandler(ExchangeRateNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleExchangeRateNotFound(
            ExchangeRateNotFoundException exception) {

        ErrorResponse error = new ErrorResponse(
                "EXCHANGE_RATE_NOT_FOUND",
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );

        return new ResponseEntity<>(
                error,
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(DuplicateExchangeRateException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateExchangeRate(
            DuplicateExchangeRateException exception) {

        ErrorResponse error = new ErrorResponse(
                "DUPLICATE_EXCHANGE_RATE",
                exception.getMessage(),
                HttpStatus.CONFLICT.value()
        );

        return new ResponseEntity<>(
                error,
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse error = new ErrorResponse(
                "VALIDATION_ERROR",
                message,
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(
                error,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(
            ResponseStatusException exception) {

        ErrorResponse error = new ErrorResponse(
                "REQUEST_ERROR",
                exception.getReason(),
                exception.getStatusCode().value()
        );

        return new ResponseEntity<>(
                error,
                exception.getStatusCode()
        );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception exception) {

        ErrorResponse error = new ErrorResponse(
                "PROCESSING_ERROR",
                exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return new ResponseEntity<>(
                error,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(PaymentFxDetailsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentFxDetailsNotFound(
            PaymentFxDetailsNotFoundException exception) {

        ErrorResponse error = new ErrorResponse(
                "PAYMENT_FX_DETAILS_NOT_FOUND",
                exception.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );

        return new ResponseEntity<>(
                error,
                HttpStatus.NOT_FOUND
        );
    }
}