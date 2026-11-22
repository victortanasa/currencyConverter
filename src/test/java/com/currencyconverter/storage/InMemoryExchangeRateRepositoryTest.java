package com.currencyconverter.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.currencyconverter.exception.UnsupportedCurrencyException;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class InMemoryExchangeRateRepositoryTest {

    private static final String EUR = "EUR";
    private static final String USD = "USD";
    private static final String GBP = "GBP";
    private static final String CAD = "CAD";
    private static final String UNKNOWN = "XYZ";

    private InMemoryExchangeRateRepository createRepository() {
        return new InMemoryExchangeRateRepository(Map.of(
                EUR, Map.of(USD, new BigDecimal("1.2"), GBP, new BigDecimal("0.8")),
                USD, Map.of(EUR, new BigDecimal("0.83"), CAD, new BigDecimal("1.3"))));
    }

    @Test
    void findsQuoteOfBase() {
        // given
        final InMemoryExchangeRateRepository repository = createRepository();

        // when / then
        assertThat(repository.findRate(EUR, USD)).contains(new BigDecimal("1.2"));
        assertThat(repository.findRate(GBP, EUR)).isEmpty();
        assertThat(repository.findRate(EUR, CAD)).isEmpty();
    }

    @Test
    void findsBasesQuotingBothCurrencies() {
        // given
        final InMemoryExchangeRateRepository repository = createRepository();

        // when / then
        assertThat(repository.findBasesQuoting(USD, GBP)).containsExactly(EUR);
        assertThat(repository.findBasesQuoting(GBP, CAD)).isEmpty();
    }

    @Test
    void recognizesBasesAndQuotedCurrenciesAsKnown() {
        // given
        final InMemoryExchangeRateRepository repository = createRepository();

        // when / then
        assertThat(repository.isKnownCurrency(EUR)).isTrue();
        assertThat(repository.isKnownCurrency(CAD)).isTrue();
        assertThat(repository.isKnownCurrency(UNKNOWN)).isFalse();
    }

    @Test
    void savesRateOverwritingExistingAndAddingNew() {
        // given
        final InMemoryExchangeRateRepository repository = createRepository();

        // when
        repository.saveRate(EUR, USD, new BigDecimal("1.5"));
        repository.saveRate(EUR, CAD, new BigDecimal("1.45"));

        // then
        assertThat(repository.findRate(EUR, USD)).contains(new BigDecimal("1.5"));
        assertThat(repository.findRate(EUR, CAD)).contains(new BigDecimal("1.45"));
    }

    @Test
    void rejectsSavingRateForUnknownBase() {
        // given
        final InMemoryExchangeRateRepository repository = createRepository();

        // when / then
        assertThatThrownBy(() -> repository.saveRate(UNKNOWN, USD, BigDecimal.ONE))
                .isInstanceOf(UnsupportedCurrencyException.class);
    }
}
