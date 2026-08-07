package com.example.payment_processing.util;

import java.util.Locale;
import java.util.Map;

public final class CurrencyCodeNormalizer {

    private static final Map<String, String> CURRENCY_ALIASES = Map.ofEntries(
            Map.entry("INR", "INR"),
            Map.entry("IN", "INR"),
            Map.entry("IND", "INR"),
            Map.entry("INDIA", "INR"),
            Map.entry("RUPEE", "INR"),
            Map.entry("RUPEES", "INR"),
            Map.entry("USD", "USD"),
            Map.entry("US", "USD"),
            Map.entry("USA", "USD"),
            Map.entry("UNITED STATES", "USD"),
            Map.entry("UNITED STATES OF AMERICA", "USD"),
            Map.entry("DOLLAR", "USD"),
            Map.entry("DOLLARS", "USD"),
            Map.entry("EUR", "EUR"),
            Map.entry("EU", "EUR"),
            Map.entry("EURO", "EUR"),
            Map.entry("EUROPE", "EUR"),
            Map.entry("GBP", "GBP"),
            Map.entry("GB", "GBP"),
            Map.entry("UK", "GBP"),
            Map.entry("GBR", "GBP"),
            Map.entry("UNITED KINGDOM", "GBP"),
            Map.entry("BRITAIN", "GBP"),
            Map.entry("GREAT BRITAIN", "GBP"),
            Map.entry("POUND", "GBP"),
            Map.entry("JPY", "JPY"),
            Map.entry("JP", "JPY"),
            Map.entry("JPN", "JPY"),
            Map.entry("JAPAN", "JPY"),
            Map.entry("YEN", "JPY"),
            Map.entry("CAD", "CAD"),
            Map.entry("CA", "CAD"),
            Map.entry("CAN", "CAD"),
            Map.entry("CANADA", "CAD")
    );

    private CurrencyCodeNormalizer() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String sanitized = value.trim().replace('-', ' ').replace('_', ' ');
        if (sanitized.isEmpty()) {
            return sanitized;
        }

        String upper = sanitized.toUpperCase(Locale.ROOT).replaceAll("\\s+", " ");
        return CURRENCY_ALIASES.getOrDefault(upper, upper);
    }
}
