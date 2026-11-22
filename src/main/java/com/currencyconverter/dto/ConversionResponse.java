package com.currencyconverter.dto;

import java.math.BigDecimal;

/**
 * Result of converting an amount from one currency to another.
 */
public record ConversionResponse(BigDecimal convertedAmount) {
}
