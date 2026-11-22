package com.currencyconverter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.currencyconverter.service.ExchangeRateProvider;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CurrencyConverterApplicationTest {

    private static final String EUR = "EUR";
    private static final String USD = "USD";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExchangeRateProvider exchangeRateProvider;

    @Test
    void loadsRatesFromProvidedFile() {
        // when
        final BigDecimal eurToUsd = exchangeRateProvider.getRate(EUR, "usd");
        final BigDecimal usdToEur = exchangeRateProvider.getRate(USD, EUR);

        // then
        assertThat(eurToUsd).isEqualByComparingTo("1.16063");
        assertThat(usdToEur).isEqualByComparingTo("0.90992");
    }

    @Test
    void convertsEurToUsdEndToEnd() throws Exception {
        // when / then
        mockMvc.perform(post("/api/conversions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceCurrency\":\"EUR\",\"targetCurrency\":\"USD\",\"amount\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.convertedAmount").value(116.06));
    }

    @Test
    void derivesRateBetweenCurrenciesWithoutDirectQuote() throws Exception {
        // AUD has no block of its own, so AUD -> SEK is derived through EUR: 11.03937 / 1.639644 * 100
        mockMvc.perform(post("/api/conversions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceCurrency\":\"AUD\",\"targetCurrency\":\"SEK\",\"amount\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.convertedAmount").value(673.28));
    }
}
