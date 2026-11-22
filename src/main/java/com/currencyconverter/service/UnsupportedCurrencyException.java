package com.currencyconverter.service;

/**
 * Thrown when a conversion is requested for a currency code that has no
 * known exchange rate.
 */
public class UnsupportedCurrencyException extends RuntimeException {

    public UnsupportedCurrencyException(final String currencyCode) {
        super("Unsupported currency: " + currencyCode);
    }
}
