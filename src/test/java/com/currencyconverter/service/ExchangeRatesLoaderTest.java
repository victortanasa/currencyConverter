package com.currencyconverter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

class ExchangeRatesLoaderTest {

    private static final String EUR = "EUR";
    private static final String USD = "USD";
    private static final String GBP = "GBP";

    private final ExchangeRatesLoader exchangeRatesLoader = new ExchangeRatesLoader(new ObjectMapper());

    @TempDir
    private Path tempDir;

    @Test
    void loadsQuotesPerBaseCurrency() {
        // given
        final Resource json = jsonOf("["
                + "{\"source\":\"EUR\",\"quotes\":{\"EURUSD\":1.16063,\"EURGBP\":0.856017}},"
                + "{\"source\":\"usd\",\"quotes\":{\"USDEUR\":0.861601}}]");

        // when
        final Map<String, Map<String, BigDecimal>> rates = exchangeRatesLoader.load(json);

        // then
        assertThat(rates).containsOnlyKeys(EUR, USD);
        assertThat(rates.get(EUR)).containsOnlyKeys(USD, GBP);
        assertThat(rates.get(EUR).get(USD)).isEqualByComparingTo("1.16063");
        assertThat(rates.get(USD).get(EUR)).isEqualByComparingTo("0.861601");
    }

    @Test
    void failsFastWhenFileIsMissing() {
        // given
        final Resource missing = new FileSystemResource(tempDir.resolve("missing.json"));

        // when / then
        assertThatThrownBy(() -> exchangeRatesLoader.load(missing))
                .isInstanceOf(UncheckedIOException.class);
    }

    @Test
    void failsFastWhenJsonIsMalformed() {
        // given
        final Resource json = jsonOf("[not json");

        // when / then
        assertThatThrownBy(() -> exchangeRatesLoader.load(json))
                .isInstanceOf(UncheckedIOException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "null",
            "[]",
            "[{\"quotes\":{\"EURUSD\":1.1}}]",
            "[{\"source\":\" \",\"quotes\":{\"EURUSD\":1.1}}]",
            "[{\"source\":\"EUR\"}]",
            "[{\"source\":\"EUR\",\"quotes\":{}}]",
            "[{\"source\":\"EUR\",\"quotes\":{\"EURUSD\":null}}]",
            "[{\"source\":\"EUR\",\"quotes\":{\"EURUSD\":0}}]",
            "[{\"source\":\"EUR\",\"quotes\":{\"EURUSD\":-1.5}}]",
            "[{\"source\":\"EUR\",\"quotes\":{\"USDGBP\":0.8}}]",
            "[{\"source\":\"EUR\",\"quotes\":{\"EUR\":1}}]",
            "[{\"source\":\"EUR\",\"quotes\":{\"EURUSD\":1.1}},{\"source\":\"EUR\",\"quotes\":{\"EURGBP\":0.8}}]"
    })
    void failsFastOnInvalidContent(final String content) {
        // given
        final Resource json = jsonOf(content);

        // when / then
        assertThatThrownBy(() -> exchangeRatesLoader.load(json))
                .isInstanceOf(IllegalStateException.class);
    }

    private static Resource jsonOf(final String content) {
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8));
    }
}
