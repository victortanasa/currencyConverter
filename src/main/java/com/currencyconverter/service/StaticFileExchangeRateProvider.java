package com.currencyconverter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Loads EUR-based rates once at startup from a JSON file and serves them from memory.
 * Fails fast if the file is missing or malformed.
 */
@Slf4j
@Component
public class StaticFileExchangeRateProvider implements ExchangeRateProvider {

    private static final String BASE_CURRENCY = "EUR";

    private final Map<String, BigDecimal> ratesPerEur;

    public StaticFileExchangeRateProvider(
            final ObjectMapper objectMapper,
            @Value("${exchange-rates.location:classpath:rates.json}") final Resource location) {
        this.ratesPerEur = load(objectMapper, location);
        log.info("Loaded {} exchange rates from {}", ratesPerEur.size(), location);
    }

    @Override
    public BigDecimal getRateFor(final String currencyCode) {
        final BigDecimal rate = ratesPerEur.get(currencyCode.trim().toUpperCase(Locale.ROOT));
        if (rate == null) {
            throw new UnsupportedCurrencyException(currencyCode);
        }
        return rate;
    }

    private static Map<String, BigDecimal> load(final ObjectMapper objectMapper, final Resource location) {
        final ExchangeRatesData data;
        try (InputStream inputStream = location.getInputStream()) {
            data = objectMapper.readValue(inputStream, ExchangeRatesData.class);
        } catch (final IOException e) {
            throw new UncheckedIOException("Unable to read exchange rates from " + location, e);
        }
        if (data.quotes() == null || data.quotes().isEmpty()) {
            throw new IllegalStateException("Exchange rates file has no usable quotes: " + location);
        }

        final Map<String, BigDecimal> rates = new HashMap<>();
        rates.put(BASE_CURRENCY, BigDecimal.ONE);
        data.quotes().forEach((pair, rate) -> {
            if (!pair.startsWith(BASE_CURRENCY)
                    || pair.length() == BASE_CURRENCY.length()
                    || rate == null
                    || rate.signum() <= 0) {
                throw new IllegalStateException("Invalid exchange rate entry: " + pair + "=" + rate);
            }
            rates.put(pair.substring(BASE_CURRENCY.length()), rate);
        });
        return Map.copyOf(rates);
    }
}
