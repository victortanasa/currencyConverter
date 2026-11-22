package com.currencyconverter.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * Loads the exchange rates once at startup into the in-memory repository.
 */
@Slf4j
@Configuration
public class ExchangeRatesConfig {

    @Bean
    public InMemoryExchangeRateRepository exchangeRateRepository(
            final ExchangeRatesLoader exchangeRatesLoader,
            @Value("${exchange-rates.location:classpath:rates.json}") final Resource location) {
        final InMemoryExchangeRateRepository repository = new InMemoryExchangeRateRepository(exchangeRatesLoader.load(location));
        log.info("Loaded rates for {} base currencies from {}", repository.baseCount(), location);
        return repository;
    }
}
