package com.currencyconverter.storage;

import com.currencyconverter.exception.UnsupportedCurrencyException;

import java.math.BigDecimal;

/**
 * Source of exchange rates between currencies.
 */
public interface ExchangeRateProvider {

    /**
     * @param sourceCurrency ISO currency code to convert from, case-insensitive
     * @param targetCurrency ISO currency code to convert to, case-insensitive
     * @return units of the target currency per 1 unit of the source currency
     * @throws UnsupportedCurrencyException if either currency is unknown or no rate can be derived
     */
    BigDecimal getRate(String sourceCurrency, String targetCurrency);
}
