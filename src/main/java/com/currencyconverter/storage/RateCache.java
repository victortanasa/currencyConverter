package com.currencyconverter.storage;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Name and key format of the rates cache, used by the resolver that fills it. Entries are keyed on the
 * normalized codes, so " eur " and "EUR" share one entry. Methods using {@link #KEY} must name their
 * parameters {@code sourceCurrency} and {@code targetCurrency}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RateCache {

    public static final String NAME = "rates";

    public static final String KEY = "T(com.currencyconverter.util.CurrencyUtils).normalize(#sourceCurrency)"
            + " + '-' + T(com.currencyconverter.util.CurrencyUtils).normalize(#targetCurrency)";
}
