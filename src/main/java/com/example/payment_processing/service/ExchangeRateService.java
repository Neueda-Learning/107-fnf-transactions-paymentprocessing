package com.example.payment_processing.service;

import com.example.payment_processing.dto.CreateExchangeRateRequest;
import com.example.payment_processing.exception.DuplicateExchangeRateException;
import com.example.payment_processing.exception.ExchangeRateNotFoundException;
import com.example.payment_processing.model.ExchangeRate;
import com.example.payment_processing.repository.ExchangeRateRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public List<ExchangeRate> getAllExchangeRates() {
        return exchangeRateRepository.findAll();
    }

    public ExchangeRate getExchangeRateByCurrency(String currencyCode) {
        String normalizedCurrencyCode = normalizeCurrencyCode(currencyCode);
        return exchangeRateRepository.findById(normalizedCurrencyCode)
                .orElseThrow(() -> new ExchangeRateNotFoundException(
                        "Exchange rate for currency " + normalizedCurrencyCode + " not found"
                ));
    }

    public ExchangeRate createExchangeRate(CreateExchangeRateRequest request) {
        String normalizedCurrencyCode = normalizeCurrencyCode(request.getCurrencyCode());

        if (exchangeRateRepository.existsById(normalizedCurrencyCode)) {
            throw new DuplicateExchangeRateException(
                    "Exchange rate for currency " + normalizedCurrencyCode + " already exists"
            );
        }

        ExchangeRate exchangeRate = new ExchangeRate();
        exchangeRate.setCurrencyCode(normalizedCurrencyCode);
        exchangeRate.setRateToUsd(request.getRateToUsd());
        exchangeRate.setUpdatedAt(LocalDateTime.now());

        return exchangeRateRepository.save(exchangeRate);
    }

    private String normalizeCurrencyCode(String currencyCode) {
        return currencyCode.trim().toUpperCase(Locale.ROOT);
    }
}

