package com.currencyconverter.service;

import com.currencyconverter.dto.ConversionRequest;
import com.currencyconverter.dto.ConversionResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Converts amounts between currencies by cross-rating through the EUR base currency.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyConversionService {

    private static final int RESULT_SCALE = 2;

    private final ExchangeRateProvider exchangeRateProvider;

    public ConversionResponse convert(final ConversionRequest request) {
        log.debug("Converting {} {} to {}", request.amount(), request.sourceCurrency(), request.targetCurrency());
        final BigDecimal sourceRate = exchangeRateProvider.getRateFor(request.sourceCurrency());
        final BigDecimal targetRate = exchangeRateProvider.getRateFor(request.targetCurrency());

        final BigDecimal convertedAmount = request.amount()
                .multiply(targetRate)
                .divide(sourceRate, RESULT_SCALE, RoundingMode.HALF_UP);

        return new ConversionResponse(convertedAmount);
    }
}
