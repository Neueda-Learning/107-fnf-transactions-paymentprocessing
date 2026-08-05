package com.example.payment_processing.repository;

import com.example.payment_processing.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, String> {
    Optional<ExchangeRate> findByCurrencyCode(String currencyCode);
}

