package com.currencyconverter.controller;

import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.currencyconverter.dto.ConversionResponse;
import com.currencyconverter.service.CurrencyConversionService;
import com.currencyconverter.exception.UnsupportedCurrencyException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CurrencyConversionController.class)
class CurrencyConversionControllerTest {

    private static final String URL = "/api/conversions";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyConversionService currencyConversionService;

    @Test
    void returnsConvertedAmount() throws Exception {
        // given
        when(currencyConversionService.convert(any())).thenReturn(new ConversionResponse(new BigDecimal("116.06")));

        // when / then
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceCurrency\":\"EUR\",\"targetCurrency\":\"USD\",\"amount\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.convertedAmount").value(116.06));
    }

    @Test
    void rejectsZeroAmount() throws Exception {
        // when / then
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceCurrency\":\"EUR\",\"targetCurrency\":\"USD\",\"amount\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value(startsWith("amount")));
    }

    @Test
    void rejectsNegativeAmount() throws Exception {
        // when / then
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceCurrency\":\"EUR\",\"targetCurrency\":\"USD\",\"amount\":-5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingFields() throws Exception {
        // when / then
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.length()").value(3));
    }

    @Test
    void rejectsMalformedJson() throws Exception {
        // when / then
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("{oops"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsBlankCurrency() throws Exception {
        // when / then
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceCurrency\":\" \",\"targetCurrency\":\"USD\",\"amount\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value(startsWith("sourceCurrency")));
    }

    @Test
    void returnsGeneric500ForUnexpectedErrors() throws Exception {
        // given
        when(currencyConversionService.convert(any())).thenThrow(new IllegalStateException("secret internal detail"));

        // when / then
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceCurrency\":\"EUR\",\"targetCurrency\":\"USD\",\"amount\":1}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("Unexpected error"));
    }

    @Test
    void rejectsUnsupportedMediaType() throws Exception {
        // when / then
        mockMvc.perform(post(URL).contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void rejectsWrongHttpMethod() throws Exception {
        // when / then
        mockMvc.perform(get(URL))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void rejectsUnsupportedCurrency() throws Exception {
        // given
        when(currencyConversionService.convert(any())).thenThrow(new UnsupportedCurrencyException("XYZ"));

        // when / then
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sourceCurrency\":\"EUR\",\"targetCurrency\":\"XYZ\",\"amount\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Unsupported currency: XYZ"));
    }
}
