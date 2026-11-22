package com.currencyconverter.storage;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps the rates in memory. The set of base currencies is fixed after loading, and each base's quotes
 * live in a concurrent map so single rates can be replaced while requests are being served.
 */
public class InMemoryExchangeRateRepository implements ExchangeRateRepository {

    private final Map<String, Map<String, BigDecimal>> quotesByBase;

    public InMemoryExchangeRateRepository(final Map<String, Map<String, BigDecimal>> quotesByBase) {
        this.quotesByBase = new LinkedHashMap<>();
        quotesByBase.forEach((base, quotes) -> this.quotesByBase.put(base, new ConcurrentHashMap<>(quotes)));
    }

    public int baseCount() {
        return quotesByBase.size();
    }

    @Override
    public Optional<BigDecimal> findRate(final String base, final String quoted) {
        return Optional.ofNullable(quotesByBase.getOrDefault(base, Map.of()).get(quoted));
    }

    @Override
    public List<String> findBasesQuoting(final String first, final String second) {
        return quotesByBase.entrySet().stream()
                .filter(entry -> entry.getValue().containsKey(first) && entry.getValue().containsKey(second))
                .map(Map.Entry::getKey)
                .toList();
    }

    @Override
    public boolean isKnownCurrency(final String currency) {
        return quotesByBase.containsKey(currency)
                || quotesByBase.values().stream().anyMatch(quotes -> quotes.containsKey(currency));
    }

    @Override
    public void saveRate(final String base, final String quoted, final BigDecimal rate) {
        final Map<String, BigDecimal> quotes = quotesByBase.get(base);
        if (quotes == null) {
            throw new IllegalArgumentException("Unknown base currency: " + base);
        }
        quotes.put(quoted, rate);
    }
}
