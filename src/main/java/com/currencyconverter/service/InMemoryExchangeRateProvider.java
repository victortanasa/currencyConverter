package com.currencyconverter.service;

import com.currencyconverter.util.CurrencyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Serves rates from memory; the file is loaded once at startup. A direct quote is used when one
 * exists. Otherwise the rate is derived through the first base currency, in file order, that quotes
 * both currencies.
 */
@Slf4j
@Component
public class InMemoryExchangeRateProvider implements ExchangeRateProvider {

    // keyed on the normalized codes, so " eur " and "EUR" share one entry
    private static final String CACHE_KEY = "T(com.currencyconverter.util.CurrencyUtils).normalize(#sourceCurrency)"
            + " + '-' + T(com.currencyconverter.util.CurrencyUtils).normalize(#targetCurrency)";

    private final Map<String, Map<String, BigDecimal>> ratesBySourceCurrency;
    private final Set<String> knownCurrencies;

    public InMemoryExchangeRateProvider(
            final ExchangeRatesLoader exchangeRatesLoader,
            @Value("${exchange-rates.location:classpath:rates.json}") final Resource location) {
        this.ratesBySourceCurrency = exchangeRatesLoader.load(location);
        this.knownCurrencies = knownCurrenciesOf(ratesBySourceCurrency);
        log.info("Loaded rates for {} base currencies from {}", ratesBySourceCurrency.size(), location);
    }

    @Override
    @Cacheable(cacheNames = "rates", key = CACHE_KEY)
    public BigDecimal getRate(final String sourceCurrency, final String targetCurrency) {
        final String source = CurrencyUtils.normalize(sourceCurrency);
        final String target = CurrencyUtils.normalize(targetCurrency);

        validateCurrency(source, sourceCurrency);
        validateCurrency(target, targetCurrency);

        if (source.equals(target)) {
            return BigDecimal.ONE;
        }

        final BigDecimal direct = ratesBySourceCurrency.getOrDefault(source, Map.of()).get(target);
        if (direct != null) {
            return direct;
        }

        return ratesBySourceCurrency.values().stream()
                .filter(quotes -> quotes.containsKey(source) && quotes.containsKey(target))
                .findFirst()
                .map(quotes -> quotes.get(target).divide(quotes.get(source), MathContext.DECIMAL128))
                .orElseThrow(() -> new UnsupportedCurrencyException(sourceCurrency, targetCurrency));
    }

    private void validateCurrency(final String normalizedCode, final String originalCode) {
        if (!knownCurrencies.contains(normalizedCode)) {
            throw new UnsupportedCurrencyException(originalCode);
        }
    }

    private static Set<String> knownCurrenciesOf(final Map<String, Map<String, BigDecimal>> ratesBySourceCurrency) {
        final Set<String> currencies = new HashSet<>(ratesBySourceCurrency.keySet());
        ratesBySourceCurrency.values().forEach(quotes -> currencies.addAll(quotes.keySet()));
        return Set.copyOf(currencies);
    }
}
