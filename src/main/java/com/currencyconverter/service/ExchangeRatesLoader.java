package com.currencyconverter.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.currencyconverter.util.CurrencyUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Reads and validates the exchange rates JSON file: a list of blocks, one per base currency,
 * each holding quotes such as {@code EURUSD} (units of USD per 1 EUR).
 */
@Component
@RequiredArgsConstructor
public class ExchangeRatesLoader {

    private final ObjectMapper objectMapper;

    /**
     * @return unmodifiable map, in file order, of base currency to (quoted currency to units per
     *     1 unit of the base)
     * @throws UncheckedIOException if the file cannot be read or parsed
     * @throws IllegalStateException if the content is empty, has a duplicate base or an invalid entry
     */
    public Map<String, Map<String, BigDecimal>> load(final Resource location) {
        final List<ExchangeRatesData> blocks = read(location);
        if (blocks == null || blocks.isEmpty()) {
            throw new IllegalStateException("Exchange rates file has no rate blocks: " + location);
        }

        final Map<String, Map<String, BigDecimal>> ratesBySourceCurrency = new LinkedHashMap<>();
        for (final ExchangeRatesData block : blocks) {
            final String base = baseOf(block, location);
            if (ratesBySourceCurrency.put(base, quotesOf(base, block)) != null) {
                throw new IllegalStateException("Duplicate base currency " + base + " in " + location);
            }
        }
        return Collections.unmodifiableMap(ratesBySourceCurrency);
    }

    private List<ExchangeRatesData> read(final Resource location) {
        try (InputStream inputStream = location.getInputStream()) {
            return objectMapper.readValue(inputStream, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, ExchangeRatesData.class));
        } catch (final IOException e) {
            throw new UncheckedIOException("Unable to read exchange rates from " + location, e);
        }
    }

    private static String baseOf(final ExchangeRatesData block, final Resource location) {
        if (block.source() == null || block.source().isBlank()) {
            throw new IllegalStateException("Rate block without a source currency in " + location);
        }
        return CurrencyUtils.normalize(block.source());
    }

    private static Map<String, BigDecimal> quotesOf(final String base, final ExchangeRatesData block) {
        if (block.quotes() == null || block.quotes().isEmpty()) {
            throw new IllegalStateException("No quotes for base currency " + base);
        }
        final Map<String, BigDecimal> quotes = new HashMap<>();
        block.quotes().forEach((pair, rate) -> quotes.put(quotedCurrencyOf(base, pair, rate), rate));
        return Map.copyOf(quotes);
    }

    private static String quotedCurrencyOf(final String base, final String pair, final BigDecimal rate) {
        final boolean validPair = pair.startsWith(base) && pair.length() > base.length();
        if (!validPair || rate == null || rate.signum() <= 0) {
            throw new IllegalStateException("Invalid exchange rate entry: " + pair + "=" + rate);
        }
        return pair.substring(base.length());
    }
}
