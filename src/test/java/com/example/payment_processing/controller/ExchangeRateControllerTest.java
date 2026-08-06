package com.example.payment_processing.controller;

import com.example.payment_processing.dto.CreateExchangeRateRequest;
import com.example.payment_processing.exception.DuplicateExchangeRateException;
import com.example.payment_processing.exception.ExchangeRateNotFoundException;
import com.example.payment_processing.model.ExchangeRate;
import com.example.payment_processing.service.ExchangeRateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateControllerTest {

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private ExchangeRateController exchangeRateController;

    @Test
    void getAllExchangeRates_shouldDelegateToService() {
        List<ExchangeRate> expected = List.of(new ExchangeRate());
        when(exchangeRateService.getAllExchangeRates()).thenReturn(expected);

        List<ExchangeRate> result = exchangeRateController.getAllExchangeRates();

        assertEquals(expected, result);
        verify(exchangeRateService).getAllExchangeRates();
    }

    @Test
    void getExchangeRateByCurrency_shouldDelegateToService() {
        ExchangeRate expected = new ExchangeRate();
        expected.setCurrencyCode("EUR");

        when(exchangeRateService.getExchangeRateByCurrency("EUR")).thenReturn(expected);

        ExchangeRate result = exchangeRateController.getExchangeRateByCurrency("EUR");

        assertEquals(expected, result);
        verify(exchangeRateService).getExchangeRateByCurrency("EUR");
    }

    @Test
    void getExchangeRateByCurrency_whenNotFound_shouldThrowExchangeRateNotFoundException() {
        when(exchangeRateService.getExchangeRateByCurrency("XYZ"))
                .thenThrow(new ExchangeRateNotFoundException("Exchange rate for currency XYZ not found"));

        assertThrows(ExchangeRateNotFoundException.class,
                () -> exchangeRateController.getExchangeRateByCurrency("XYZ"));
    }

    @Test
    void createExchangeRate_shouldDelegateToService() {
        CreateExchangeRateRequest request = new CreateExchangeRateRequest();
        request.setCurrencyCode("USD");
        request.setRateToUsd(new BigDecimal("1.000000"));

        ExchangeRate expected = new ExchangeRate();
        expected.setCurrencyCode("USD");

        when(exchangeRateService.createExchangeRate(request)).thenReturn(expected);

        ExchangeRate result = exchangeRateController.createExchangeRate(request);

        assertEquals(expected, result);
        verify(exchangeRateService).createExchangeRate(request);
    }

    @Test
    void createExchangeRate_whenDuplicate_shouldThrowDuplicateExchangeRateException() {
        when(exchangeRateService.createExchangeRate(any()))
                .thenThrow(new DuplicateExchangeRateException("Exchange rate for currency USD already exists"));

        assertThrows(DuplicateExchangeRateException.class,
                () -> exchangeRateController.createExchangeRate(new CreateExchangeRateRequest()));
    }
}
