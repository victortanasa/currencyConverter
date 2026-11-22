package com.currencyconverter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.currencyconverter.dto.ConversionRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionServiceTest {

    private static final String EUR = "EUR";
    private static final String USD = "USD";
    private static final String GBP = "GBP";
    private static final String UNKNOWN = "XYZ";

    private static final BigDecimal EUR_RATE = BigDecimal.ONE;
    private static final BigDecimal USD_RATE = new BigDecimal("1.16063");
    private static final BigDecimal GBP_RATE = new BigDecimal("0.856017");

    @Mock
    private ExchangeRateProvider exchangeRateProvider;

    @InjectMocks
    private CurrencyConversionService currencyConversionService;

    @Test
    void convertsFromBaseCurrency() {
        // given
        when(exchangeRateProvider.getRateFor(EUR)).thenReturn(EUR_RATE);
        when(exchangeRateProvider.getRateFor(USD)).thenReturn(USD_RATE);
        final ConversionRequest request = new ConversionRequest(EUR, USD, new BigDecimal("100"));

        // when
        final BigDecimal result = currencyConversionService.convert(request).convertedAmount();

        // then
        assertThat(result).isEqualByComparingTo("116.06");
    }

    @Test
    void convertsToBaseCurrency() {
        // given
        when(exchangeRateProvider.getRateFor(USD)).thenReturn(USD_RATE);
        when(exchangeRateProvider.getRateFor(EUR)).thenReturn(EUR_RATE);
        final ConversionRequest request = new ConversionRequest(USD, EUR, new BigDecimal("100"));

        // when
        final BigDecimal result = currencyConversionService.convert(request).convertedAmount();

        // then
        assertThat(result).isEqualByComparingTo("86.16");
    }

    @Test
    void convertsBetweenTwoNonBaseCurrencies() {
        // given
        when(exchangeRateProvider.getRateFor(USD)).thenReturn(USD_RATE);
        when(exchangeRateProvider.getRateFor(GBP)).thenReturn(GBP_RATE);
        final ConversionRequest request = new ConversionRequest(USD, GBP, new BigDecimal("100"));

        // when
        final BigDecimal result = currencyConversionService.convert(request).convertedAmount();

        // then
        assertThat(result).isEqualByComparingTo("73.75");
    }

    @Test
    void sameCurrencyReturnsSameAmount() {
        // given
        when(exchangeRateProvider.getRateFor(USD)).thenReturn(USD_RATE);
        final ConversionRequest request = new ConversionRequest(USD, USD, new BigDecimal("42.50"));

        // when
        final BigDecimal result = currencyConversionService.convert(request).convertedAmount();

        // then
        assertThat(result).isEqualByComparingTo("42.50");
    }

    @Test
    void roundsHalfUpToTwoDecimals() {
        // given
        when(exchangeRateProvider.getRateFor(EUR)).thenReturn(EUR_RATE);
        when(exchangeRateProvider.getRateFor(USD)).thenReturn(USD_RATE);
        final ConversionRequest request = new ConversionRequest(EUR, USD, new BigDecimal("0.01"));

        // when
        final BigDecimal result = currencyConversionService.convert(request).convertedAmount();

        // then
        assertThat(result).isEqualByComparingTo("0.01");
        assertThat(result.scale()).isEqualTo(2);
    }

    @Test
    void rejectsUnsupportedCurrency() {
        // given
        when(exchangeRateProvider.getRateFor(EUR)).thenReturn(EUR_RATE);
        when(exchangeRateProvider.getRateFor(UNKNOWN)).thenThrow(new UnsupportedCurrencyException(UNKNOWN));
        final ConversionRequest request = new ConversionRequest(EUR, UNKNOWN, new BigDecimal("100"));

        // when / then
        assertThatThrownBy(() -> currencyConversionService.convert(request))
                .isInstanceOf(UnsupportedCurrencyException.class)
                .hasMessageContaining(UNKNOWN);
    }
}
