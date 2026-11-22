package com.currencyconverter.service;

import com.currencyconverter.exception.UnsupportedCurrencyException;
import com.currencyconverter.storage.ExchangeRateRepository;
import com.currencyconverter.storage.RateCache;
import com.currencyconverter.util.CurrencyUtils;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * Replaces the stored rate of a single currency pair.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    /**
     * Stores the rate and evicts the cached entry of that pair. The eviction runs only if the update succeeds.
     * Rates derived from this pair and cached under other keys stay until the cache entry expires.
     *
     * @throws UnsupportedCurrencyException if either currency is unknown, the two are equal, or the source
     *     currency has no block of its own to hold the quote
     */
    @CacheEvict(cacheNames = RateCache.NAME, key = RateCache.KEY)
    public void updateRate(final String sourceCurrency, final String targetCurrency, final BigDecimal rate) {
        final String source = CurrencyUtils.normalize(sourceCurrency);
        final String target = CurrencyUtils.normalize(targetCurrency);

        validateCurrency(source, sourceCurrency);
        validateCurrency(target, targetCurrency);
        if (source.equals(target)) {
            throw new UnsupportedCurrencyException(sourceCurrency, targetCurrency);
        }

        exchangeRateRepository.saveRate(source, target, rate);
        //TODO: save to "DB", right now it only saves to the in-memory map, so it will be lost on restart
        log.info("Updated rate {}/{} to {}", source, target, rate);
    }

    private void validateCurrency(final String normalizedCode, final String originalCode) {
        if (!exchangeRateRepository.isKnownCurrency(normalizedCode)) {
            throw new UnsupportedCurrencyException(originalCode);
        }
    }
}
