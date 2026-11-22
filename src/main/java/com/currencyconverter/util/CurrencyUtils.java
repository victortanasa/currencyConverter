package com.currencyconverter.util;

import java.util.Locale;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Helpers for working with currency codes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CurrencyUtils {

    /**
     * Trims and upper-cases a currency code so lookups are case- and whitespace-insensitive.
     */
    public static String normalize(final String currencyCode) {
        return currencyCode.trim().toUpperCase(Locale.ROOT);
    }
}
