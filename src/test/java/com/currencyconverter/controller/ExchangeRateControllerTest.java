package com.currencyconverter.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.currencyconverter.exception.UnsupportedCurrencyException;
import com.currencyconverter.service.ExchangeRateService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExchangeRateController.class)
class ExchangeRateControllerTest {

    private static final String URL = "/api/exchange-rates/EUR/USD";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @Test
    void updatesRate() throws Exception {
        // when / then
        mockMvc.perform(put(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rate\":1.25}"))
                .andExpect(status().isNoContent());
        verify(exchangeRateService).updateRate("EUR", "USD", new BigDecimal("1.25"));
    }

    @Test
    void rejectsNonPositiveRate() throws Exception {
        // when / then
        mockMvc.perform(put(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rate\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("rate: must be greater than 0"));
    }

    @Test
    void rejectsMissingRate() throws Exception {
        // when / then
        mockMvc.perform(put(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsBadRequestForUnsupportedCurrency() throws Exception {
        // given
        doThrow(new UnsupportedCurrencyException("USD")).when(exchangeRateService).updateRate(any(), any(), any());

        // when / then
        mockMvc.perform(put(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rate\":1.25}"))
                .andExpect(status().isBadRequest());
    }
}
