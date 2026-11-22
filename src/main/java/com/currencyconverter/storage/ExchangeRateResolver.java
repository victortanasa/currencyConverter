package com.currencyconverter.storage;

import com.currencyconverter.exception.UnsupportedCurrencyException;
import com.currencyconverter.util.CurrencyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;

/**
 * Resolves the rate between two currencies from the repository. A direct quote is used when one
 * exists. Otherwise the rate is derived through the first base currency, in load order, that quotes
 * both currencies.
 */
@Component
@RequiredArgsConstructor
public class ExchangeRateResolver implements ExchangeRateProvider {

    private final ExchangeRateRepository exchangeRateRepository;

    @Override
    @Cacheable(cacheNames = RateCache.NAME, key = RateCache.KEY)
    public BigDecimal getRate(final String sourceCurrency, final String targetCurrency) {
        final String source = CurrencyUtils.normalize(sourceCurrency);
        final String target = CurrencyUtils.normalize(targetCurrency);

        validateCurrency(source, sourceCurrency);
        validateCurrency(target, targetCurrency);

        if (source.equals(target)) {
            return BigDecimal.ONE;
        }

        return exchangeRateRepository.findRate(source, target)
                .or(() -> crossRate(source, target))
                .orElseThrow(() -> new UnsupportedCurrencyException(sourceCurrency, targetCurrency));
    }

    /**
     * Cross rate through the first base currency that quotes both: target per base / source per base.
     */
    private Optional<BigDecimal> crossRate(final String source, final String target) {
        return exchangeRateRepository.findBasesQuoting(source, target).stream()
                .findFirst()
                .flatMap(base -> exchangeRateRepository.findRate(base, target)
                        .flatMap(targetPerBase -> exchangeRateRepository.findRate(base, source)
                                .map(sourcePerBase -> targetPerBase.divide(sourcePerBase, MathContext.DECIMAL128))));
    }

    private void validateCurrency(final String normalizedCode, final String originalCode) {
        if (!exchangeRateRepository.isKnownCurrency(normalizedCode)) {
            throw new UnsupportedCurrencyException(originalCode);
        }
    }
}
