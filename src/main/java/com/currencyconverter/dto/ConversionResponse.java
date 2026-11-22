package com.currencyconverter.dto;

/**
 * Result of converting an amount from one currency to another.
 */
public record ConversionResponse(
        String fromCurrency,
        String toCurrency,
        double originalAmount,
        double convertedAmount) {
}
