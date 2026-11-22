package com.currencyconverter.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * Request payload for replacing the rate of one currency pair.
 */
public record UpdateRateRequest(@NotNull @Positive BigDecimal rate) {
}
