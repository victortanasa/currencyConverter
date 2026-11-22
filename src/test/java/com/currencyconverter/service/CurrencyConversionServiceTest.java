package com.currencyconverter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import com.currencyconverter.dto.ConversionRequest;
import com.currencyconverter.dto.ConversionResponse;
import org.junit.jupiter.api.Test;

class CurrencyConversionServiceTest {

    private final CurrencyConversionService currencyConversionService = new CurrencyConversionService();

    @Test
    void convertsBetweenKnownCurrencies() {
        // given
        final ConversionRequest request = new ConversionRequest("USD", "EUR", 100);

        // when
        final ConversionResponse response = currencyConversionService.convert(request);

        // then
        assertThat(response.convertedAmount()).isCloseTo(92.5926, within(0.01));
    }

    @Test
    void rejectsUnsupportedCurrency() {
        // given
        final ConversionRequest request = new ConversionRequest("USD", "XYZ", 100);

        // when / then
        assertThatThrownBy(() -> currencyConversionService.convert(request))
                .isInstanceOf(UnsupportedCurrencyException.class)
                .hasMessageContaining("XYZ");
    }
}
