package com.currencyconverter.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Request payload for a currency conversion.
 */
public record ConversionRequest(
        @NotBlank String sourceCurrency,
        @NotBlank String targetCurrency,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal amount) {
}
