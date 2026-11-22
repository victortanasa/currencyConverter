package com.currencyconverter.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for a currency conversion.
 */
public record ConversionRequest(
        @NotBlank String fromCurrency,
        @NotBlank String toCurrency,
        @DecimalMin(value = "0.0", inclusive = false) double amount) {
}
