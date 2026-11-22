package com.currencyconverter.storage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Storage of exchange rates, grouped by base currency. Currency codes are expected to be normalized.
 */
public interface ExchangeRateRepository {

    /**
     * @return the quote published for {@code quoted} against {@code base}, e.g. EURUSD for (EUR, USD)
     */
    Optional<BigDecimal> findRate(String base, String quoted);

    /**
     * @return the base currencies that quote both currencies, in the order they were loaded
     */
    List<String> findBasesQuoting(String first, String second);

    boolean isKnownCurrency(String currency);

    /**
     * Replaces the quote of {@code quoted} against {@code base}.
     *
     * @throws IllegalArgumentException if {@code base} is not a known base currency
     */
    void saveRate(String base, String quoted, BigDecimal rate);
}
