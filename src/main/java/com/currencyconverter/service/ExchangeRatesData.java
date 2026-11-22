package com.currencyconverter.service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Shape of the provided exchange rates JSON file.
 */
record ExchangeRatesData(String source, Map<String, BigDecimal> quotes) {
}
