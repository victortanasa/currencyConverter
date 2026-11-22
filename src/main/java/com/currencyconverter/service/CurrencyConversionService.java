package com.currencyconverter.service;

import com.currencyconverter.dto.ConversionRequest;
import com.currencyconverter.dto.ConversionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;

import javax.money.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.currencyconverter.util.CurrencyUtils.normalize;

/**
 * Converts amounts between currencies using the rate supplied by an {@link ExchangeRateProvider}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyConversionService {

    private final ExchangeRateProvider exchangeRateProvider;

    public ConversionResponse convert(final ConversionRequest request) {
        log.debug("Converting {} {} to {}", request.amount(), request.sourceCurrency(), request.targetCurrency());
        final BigDecimal rate = exchangeRateProvider.getRate(request.sourceCurrency(), request.targetCurrency());

        // Why MonetaryAmount (Moneta) instead of a bare BigDecimal:
        // - rounding comes from the currency: Monetary.getRounding(...) rounds to the target currency's
        //   minor units (EUR 2, JPY 0, KWD 3) instead of a hard-coded 2 decimals
        // - Money keeps up to 256 digits while multiplying, so nothing is lost before the single final rounding
        // - currency codes are validated against ISO 4217
        final CurrencyUnit target = Monetary.getCurrency(normalize(request.targetCurrency()));
        final MonetaryAmount source = Money.of(request.amount(), normalize(request.sourceCurrency()));

        final MonetaryAmount converted = Money.of(source.multiply(rate).getNumber(), target).with(roundingFor(target));

        // Moneta strips trailing zeros (100.00 becomes 1E+2), so restore the currency's scale for the response
        final BigDecimal convertedAmount = converted.getNumber()
                .numberValueExact(BigDecimal.class)
                .setScale(target.getDefaultFractionDigits(), RoundingMode.UNNECESSARY);

        return new ConversionResponse(convertedAmount);
    }

    private static MonetaryOperator roundingFor(final CurrencyUnit currency) {
        return Monetary.getRounding(RoundingQueryBuilder.of()
                .setCurrency(currency)
                .set(RoundingMode.HALF_UP)
                .build());
    }

}
