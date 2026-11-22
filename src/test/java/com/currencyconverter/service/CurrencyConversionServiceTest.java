package com.currencyconverter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.currencyconverter.dto.ConversionRequest;
import java.math.BigDecimal;

import com.currencyconverter.exception.UnsupportedCurrencyException;
import com.currencyconverter.storage.ExchangeRateProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CurrencyConversionServiceTest {

    private static final String EUR = "EUR";
    private static final String USD = "USD";
    private static final String JPY = "JPY";
    private static final String UNKNOWN = "XYZ";

    @Mock
    private ExchangeRateProvider exchangeRateProvider;

    @InjectMocks
    private CurrencyConversionService currencyConversionService;

    @Test
    void multipliesAmountByRate() {
        // given
        when(exchangeRateProvider.getRate(EUR, USD)).thenReturn(new BigDecimal("1.16063"));
        final ConversionRequest request = new ConversionRequest(EUR, USD, new BigDecimal("100"));

        // when
        final BigDecimal result = currencyConversionService.convert(request).convertedAmount();

        // then
        assertThat(result).isEqualByComparingTo("116.06");
    }

    @Test
    void roundsHalfUpToTwoDecimals() {
        // given
        when(exchangeRateProvider.getRate(EUR, USD)).thenReturn(new BigDecimal("1.5"));
        final ConversionRequest request = new ConversionRequest(EUR, USD, new BigDecimal("0.01"));

        // when
        final BigDecimal result = currencyConversionService.convert(request).convertedAmount();

        // then
        assertThat(result).isEqualByComparingTo("0.02");
        assertThat(result.scale()).isEqualTo(2);
    }

    @Test
    void roundsToTargetCurrencyMinorUnits() {
        // given
        when(exchangeRateProvider.getRate(EUR, JPY)).thenReturn(new BigDecimal("160.555"));
        final ConversionRequest request = new ConversionRequest(EUR, JPY, new BigDecimal("1"));

        // when
        final BigDecimal result = currencyConversionService.convert(request).convertedAmount();

        // then
        assertThat(result).isEqualByComparingTo("161");
        assertThat(result.scale()).isZero();
    }

    @Test
    void keepsTwoDecimalsForWholeAmounts() {
        // given
        when(exchangeRateProvider.getRate(EUR, EUR)).thenReturn(BigDecimal.ONE);
        final ConversionRequest request = new ConversionRequest(EUR, EUR, new BigDecimal("100"));

        // when
        final BigDecimal result = currencyConversionService.convert(request).convertedAmount();

        // then
        assertThat(result.toString()).isEqualTo("100.00");
    }

    @Test
    void sameCurrencyReturnsSameAmount() {
        // given
        when(exchangeRateProvider.getRate(USD, USD)).thenReturn(BigDecimal.ONE);
        final ConversionRequest request = new ConversionRequest(USD, USD, new BigDecimal("42.50"));

        // when
        final BigDecimal result = currencyConversionService.convert(request).convertedAmount();

        // then
        assertThat(result).isEqualByComparingTo("42.50");
    }

    @Test
    void propagatesUnsupportedCurrency() {
        // given
        when(exchangeRateProvider.getRate(EUR, UNKNOWN)).thenThrow(new UnsupportedCurrencyException(UNKNOWN));
        final ConversionRequest request = new ConversionRequest(EUR, UNKNOWN, new BigDecimal("100"));

        // when / then
        assertThatThrownBy(() -> currencyConversionService.convert(request))
                .isInstanceOf(UnsupportedCurrencyException.class)
                .hasMessageContaining(UNKNOWN);
    }
}
