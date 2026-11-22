package com.currencyconverter.exception;

/**
 * Thrown when a conversion is requested for a currency, or a currency pair,
 * that has no known exchange rate.
 */
public class UnsupportedCurrencyException extends RuntimeException {

    public UnsupportedCurrencyException(final String currencyCode) {
        super("Unsupported currency: " + currencyCode);
    }

    public UnsupportedCurrencyException(final String sourceCurrency, final String targetCurrency) {
        super("No exchange rate available for pair: " + sourceCurrency + "/" + targetCurrency);
    }
}
