package com.currencyconverter.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import com.currencyconverter.exception.UnsupportedCurrencyException;
import com.currencyconverter.storage.ExchangeRateProvider;
import com.currencyconverter.storage.RateCache;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

@SpringBootTest
class ExchangeRateCachingTest {

    private static final String RATES_CACHE = RateCache.NAME;

    @Autowired
    private ExchangeRateProvider exchangeRateProvider;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Test
    void cachesRatesUnderNormalizedKey() {
        // given
        final Cache cache = cacheManager.getCache(RATES_CACHE);
        cache.clear();

        // when
        final BigDecimal first = exchangeRateProvider.getRate(" eur ", "usd");
        final BigDecimal second = exchangeRateProvider.getRate("EUR", "USD");

        // then
        assertThat(second).isEqualTo(first);
        assertThat(cache.get("EUR-USD", BigDecimal.class)).isEqualTo(first);
    }

    @Test
    void doesNotCacheUnsupportedCurrencies() {
        // given
        final Cache cache = cacheManager.getCache(RATES_CACHE);
        cache.clear();

        // when
        try {
            exchangeRateProvider.getRate("EUR", "XYZ");
        } catch (final UnsupportedCurrencyException expected) {
            // expected
        }

        // then
        assertThat(cache.get("EUR-XYZ")).isNull();
    }

    @Test
    void updatingRateEvictsOnlyThatEntryAndServesNewRate() {
        // given
        final Cache cache = cacheManager.getCache(RATES_CACHE);
        cache.clear();
        final BigDecimal original = exchangeRateProvider.getRate("EUR", "USD");
        exchangeRateProvider.getRate("EUR", "GBP");
        final BigDecimal updated = new BigDecimal("1.5");

        try {
            // when
            exchangeRateService.updateRate("eur", "usd", updated);

            // then
            assertThat(cache.get("EUR-USD")).isNull();
            assertThat(cache.get("EUR-GBP")).isNotNull();
            assertThat(exchangeRateProvider.getRate("EUR", "USD")).isEqualByComparingTo(updated);
        } finally {
            exchangeRateService.updateRate("EUR", "USD", original);
        }
    }
}
