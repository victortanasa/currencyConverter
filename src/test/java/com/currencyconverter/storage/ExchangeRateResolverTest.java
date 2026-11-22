package com.currencyconverter.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Map;

import com.currencyconverter.exception.UnsupportedCurrencyException;
import org.junit.jupiter.api.Test;

class ExchangeRateResolverTest {

    private static final String EUR = "EUR";
    private static final String USD = "USD";
    private static final String GBP = "GBP";
    private static final String AUD = "AUD";
    private static final String CAD = "CAD";
    private static final String UNKNOWN = "XYZ";

    private ExchangeRateResolver createProvider() {
        return new ExchangeRateResolver(new InMemoryExchangeRateRepository(Map.of(
                EUR, Map.of(USD, new BigDecimal("1.2"), GBP, new BigDecimal("0.8"), AUD, new BigDecimal("1.6")),
                USD, Map.of(EUR, new BigDecimal("0.83"), CAD, new BigDecimal("1.3")))));
    }

    @Test
    void returnsDirectQuoteIgnoringCaseAndWhitespace() {
        // given
        final ExchangeRateResolver provider = createProvider();

        // when
        final BigDecimal rate = provider.getRate(" eur ", "usd");

        // then
        assertThat(rate).isEqualByComparingTo("1.2");
    }

    @Test
    void prefersDirectQuoteOverDerivedRate() {
        // given
        final ExchangeRateResolver provider = createProvider();

        // when
        final BigDecimal rate = provider.getRate(USD, EUR);

        // then
        assertThat(rate).isEqualByComparingTo("0.83");
    }

    @Test
    void derivesRateThroughBaseCurrencyWhenNoDirectQuote() {
        // given
        final ExchangeRateResolver provider = createProvider();

        // when
        final BigDecimal rate = provider.getRate(USD, GBP);

        // then
        assertThat(rate).isEqualByComparingTo("0.6666666666666666666666666666666667");
    }

    @Test
    void sameCurrencyHasRateOne() {
        // given
        final ExchangeRateResolver provider = createProvider();

        // when
        final BigDecimal rate = provider.getRate(GBP, GBP);

        // then
        assertThat(rate).isEqualByComparingTo("1");
    }

    @Test
    void rejectsUnknownSourceCurrency() {
        // given
        final ExchangeRateResolver provider = createProvider();

        // when / then
        assertThatThrownBy(() -> provider.getRate(UNKNOWN, USD))
                .isInstanceOf(UnsupportedCurrencyException.class)
                .hasMessageContaining(UNKNOWN);
    }

    @Test
    void rejectsUnknownTargetCurrency() {
        // given
        final ExchangeRateResolver provider = createProvider();

        // when / then
        assertThatThrownBy(() -> provider.getRate(USD, UNKNOWN))
                .isInstanceOf(UnsupportedCurrencyException.class)
                .hasMessageContaining(UNKNOWN);
    }

    @Test
    void rejectsKnownCurrenciesWithoutCommonBase() {
        // given
        final ExchangeRateResolver provider = createProvider();

        // when / then
        assertThatThrownBy(() -> provider.getRate(AUD, CAD))
                .isInstanceOf(UnsupportedCurrencyException.class)
                .hasMessageContaining("AUD/CAD");
    }
}
