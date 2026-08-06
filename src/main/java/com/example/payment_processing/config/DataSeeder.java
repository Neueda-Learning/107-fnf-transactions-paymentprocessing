package com.example.payment_processing.config;

import com.example.payment_processing.model.ExchangeRate;
import com.example.payment_processing.repository.ExchangeRateRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Seeds exchange rates on startup if they do not already exist.
 * All rates are expressed as "1 unit of this currency = X USD".
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private final ExchangeRateRepository exchangeRateRepository;

    public DataSeeder(ExchangeRateRepository exchangeRateRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedRate("USD", "1.000000");   // 1 USD = 1.00 USD
        seedRate("INR", "0.012000");   // 1 INR = 0.012 USD
        seedRate("GBP", "1.276596");   // 1 GBP ≈ 1.2766 USD  (aligned with 1 INR = 0.0094 GBP)
        seedRate("EUR", "1.090909");   // 1 EUR ≈ 1.0909 USD  (aligned with 1 INR = 0.011 EUR)
    }

    private void seedRate(String code, String rate) {
        if (exchangeRateRepository.findByCurrencyCode(code).isEmpty()) {
            ExchangeRate er = new ExchangeRate();
            er.setCurrencyCode(code);
            er.setRateToUsd(new BigDecimal(rate));
            er.setUpdatedAt(LocalDateTime.now());
            exchangeRateRepository.save(er);
            System.out.println("[DataSeeder] Seeded exchange rate: " + code + " = " + rate + " USD");
        }
    }
}

