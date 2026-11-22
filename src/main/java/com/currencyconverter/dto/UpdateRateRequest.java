package com.currencyconverter.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Request payload for replacing the rate of one currency pair.
 */
public record UpdateRateRequest(@NotNull @Positive @Digits(integer = 6, fraction = 8) BigDecimal rate) {
}
