package com.example.payment_processing.service;

import com.example.payment_processing.dto.CreateExchangeRateRequest;
import com.example.payment_processing.exception.DuplicateExchangeRateException;
import com.example.payment_processing.exception.ExchangeRateNotFoundException;
import com.example.payment_processing.model.ExchangeRate;
import com.example.payment_processing.repository.ExchangeRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    void createExchangeRate_shouldNormalizeCurrencyAndPersistRate() {
        CreateExchangeRateRequest request = new CreateExchangeRateRequest();
        request.setCurrencyCode("eur");
        request.setRateToUsd(new BigDecimal("1.100000"));

        when(exchangeRateRepository.existsById("EUR")).thenReturn(false);
        when(exchangeRateRepository.save(any(ExchangeRate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExchangeRate saved = exchangeRateService.createExchangeRate(request);

        assertEquals("EUR", saved.getCurrencyCode());
        assertEquals(new BigDecimal("1.100000"), saved.getRateToUsd());
        assertNotNull(saved.getUpdatedAt());

        ArgumentCaptor<ExchangeRate> exchangeRateCaptor = ArgumentCaptor.forClass(ExchangeRate.class);
        verify(exchangeRateRepository).save(exchangeRateCaptor.capture());
        assertEquals("EUR", exchangeRateCaptor.getValue().getCurrencyCode());
    }

    @Test
    void createExchangeRate_shouldThrowWhenCurrencyAlreadyExists() {
        CreateExchangeRateRequest request = new CreateExchangeRateRequest();
        request.setCurrencyCode("usd");
        request.setRateToUsd(new BigDecimal("1.000000"));

        when(exchangeRateRepository.existsById("USD")).thenReturn(true);

        assertThrows(DuplicateExchangeRateException.class,
                () -> exchangeRateService.createExchangeRate(request));
    }

    @Test
    void getExchangeRateByCurrency_shouldThrowWhenMissingCurrency() {
        when(exchangeRateRepository.findById("GBP")).thenReturn(Optional.empty());

        assertThrows(ExchangeRateNotFoundException.class,
                () -> exchangeRateService.getExchangeRateByCurrency("gbp"));
    }
}

