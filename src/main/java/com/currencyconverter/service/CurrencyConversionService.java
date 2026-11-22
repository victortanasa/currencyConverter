package com.currencyconverter.service;

import com.currencyconverter.dto.ConversionRequest;
import com.currencyconverter.dto.ConversionResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Converts amounts between currencies using a fixed, in-memory table of
 * exchange rates expressed relative to USD.
 */
@Service
public class CurrencyConversionService {

    private static final Map<String, BigDecimal> RATES_TO_USD = Map.of(
            "USD", BigDecimal.ONE,
            "EUR", new BigDecimal("1.08"),
            "GBP", new BigDecimal("1.27"),
            "RON", new BigDecimal("0.22"),
            "JPY", new BigDecimal("0.0068"));

    public ConversionResponse convert(final ConversionRequest request) {
        final BigDecimal fromRate = rateFor(request.fromCurrency());
        final BigDecimal toRate = rateFor(request.toCurrency());

        final BigDecimal amountInUsd = BigDecimal.valueOf(request.amount()).multiply(fromRate);
        final BigDecimal convertedAmount = amountInUsd.divide(toRate, 4, RoundingMode.HALF_UP);

        return new ConversionResponse(
                request.fromCurrency(),
                request.toCurrency(),
                request.amount(),
                convertedAmount.doubleValue());
    }

    private BigDecimal rateFor(final String currencyCode) {
        final BigDecimal rate = RATES_TO_USD.get(currencyCode.toUpperCase());
        if (rate == null) {
            throw new UnsupportedCurrencyException(currencyCode);
        }
        return rate;
    }
}
