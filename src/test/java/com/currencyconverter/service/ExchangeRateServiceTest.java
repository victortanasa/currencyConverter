package com.currencyconverter.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.currencyconverter.exception.UnsupportedCurrencyException;
import com.currencyconverter.storage.ExchangeRateRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    private static final String EUR = "EUR";
    private static final String USD = "USD";
    private static final String UNKNOWN = "XYZ";
    private static final BigDecimal RATE = new BigDecimal("1.2");

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    void savesRateUnderNormalizedCodes() {
        // given
        when(exchangeRateRepository.isKnownCurrency(EUR)).thenReturn(true);
        when(exchangeRateRepository.isKnownCurrency(USD)).thenReturn(true);

        // when
        exchangeRateService.updateRate(" eur ", "usd", RATE);

        // then
        verify(exchangeRateRepository).saveRate(EUR, USD, RATE);
    }

    @Test
    void rejectsUnknownSourceCurrency() {
        // given
        when(exchangeRateRepository.isKnownCurrency(UNKNOWN)).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> exchangeRateService.updateRate(UNKNOWN, USD, RATE))
                .isInstanceOf(UnsupportedCurrencyException.class)
                .hasMessageContaining(UNKNOWN);
        verify(exchangeRateRepository, never()).saveRate(UNKNOWN, USD, RATE);
    }

    @Test
    void rejectsUnknownTargetCurrency() {
        // given
        when(exchangeRateRepository.isKnownCurrency(EUR)).thenReturn(true);
        when(exchangeRateRepository.isKnownCurrency(UNKNOWN)).thenReturn(false);

        // when / then
        assertThatThrownBy(() -> exchangeRateService.updateRate(EUR, UNKNOWN, RATE))
                .isInstanceOf(UnsupportedCurrencyException.class)
                .hasMessageContaining(UNKNOWN);
    }

    @Test
    void rejectsSameCurrencyPair() {
        // given
        when(exchangeRateRepository.isKnownCurrency(EUR)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> exchangeRateService.updateRate(EUR, EUR, RATE))
                .isInstanceOf(UnsupportedCurrencyException.class);
        verify(exchangeRateRepository, never()).saveRate(EUR, EUR, RATE);
    }
}
