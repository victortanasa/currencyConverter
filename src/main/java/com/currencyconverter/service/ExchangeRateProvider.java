package com.currencyconverter.service;

import java.math.BigDecimal;

/**
 * Source of EUR-based exchange rates.
 */
public interface ExchangeRateProvider {

    /**
     * @param currencyCode ISO currency code, case-insensitive
     * @return units of the given currency per 1 EUR
     * @throws UnsupportedCurrencyException if no rate is known for the currency
     */
    BigDecimal getRateFor(String currencyCode);
}
