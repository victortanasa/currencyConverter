package com.currencyconverter.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Serves rates from memory; the file is loaded once at startup. A direct quote is used when one
 * exists. Otherwise the rate is derived through the first base currency, in file order, that quotes
 * both currencies.
 */
@Slf4j
@Component
public class StaticFileExchangeRateProvider implements ExchangeRateProvider {

    private final Map<String, Map<String, BigDecimal>> ratesBySourceCurrency;
    private final Set<String> knownCurrencies;

    public StaticFileExchangeRateProvider(
            final ExchangeRatesLoader exchangeRatesLoader,
            @Value("${exchange-rates.location:classpath:rates.json}") final Resource location) {
        this.ratesBySourceCurrency = exchangeRatesLoader.load(location);
        this.knownCurrencies = knownCurrenciesOf(ratesBySourceCurrency);
        log.info("Loaded rates for {} base currencies from {}", ratesBySourceCurrency.size(), location);
    }

    @Override
    public BigDecimal getRate(final String sourceCurrency, final String targetCurrency) {
        final String source = normalize(sourceCurrency);
        final String target = normalize(targetCurrency);

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

    private static String normalize(final String currencyCode) {
        return currencyCode.trim().toUpperCase(Locale.ROOT);
    }

    private static Set<String> knownCurrenciesOf(final Map<String, Map<String, BigDecimal>> ratesBySourceCurrency) {
        final Set<String> currencies = new HashSet<>(ratesBySourceCurrency.keySet());
        ratesBySourceCurrency.values().forEach(quotes -> currencies.addAll(quotes.keySet()));
        return Set.copyOf(currencies);
    }
}
