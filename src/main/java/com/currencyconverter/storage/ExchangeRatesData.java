package com.currencyconverter.storage;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Shape of the provided exchange rates JSON file.
 */
record ExchangeRatesData(String source, Map<String, BigDecimal> quotes) {
}
