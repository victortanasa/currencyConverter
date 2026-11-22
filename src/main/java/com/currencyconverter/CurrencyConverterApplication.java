package com.currencyconverter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class CurrencyConverterApplication {

    public static void main(final String[] args) {
        SpringApplication.run(CurrencyConverterApplication.class, args);
    }
}
