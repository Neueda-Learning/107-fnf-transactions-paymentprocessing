package com.example.payment_processing.controller;

import com.example.payment_processing.dto.CreateExchangeRateRequest;
import com.example.payment_processing.model.ExchangeRate;
import com.example.payment_processing.service.ExchangeRateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exchange-rates")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    public List<ExchangeRate> getAllExchangeRates() {
        return exchangeRateService.getAllExchangeRates();
    }

    @GetMapping("/{currencyCode}")
    public ExchangeRate getExchangeRateByCurrency(@PathVariable String currencyCode) {
        return exchangeRateService.getExchangeRateByCurrency(currencyCode);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExchangeRate createExchangeRate(@Valid @RequestBody CreateExchangeRateRequest request) {
        return exchangeRateService.createExchangeRate(request);
    }
}

