package com.currencyconverter.controller;

import com.currencyconverter.dto.UpdateRateRequest;
import com.currencyconverter.service.ExchangeRateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchange-rates")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @PutMapping("/{sourceCurrency}/{targetCurrency}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRate(
            @PathVariable final String sourceCurrency,
            @PathVariable final String targetCurrency,
            @Valid @RequestBody final UpdateRateRequest request) {
        exchangeRateService.updateRate(sourceCurrency, targetCurrency, request.rate());
    }
}
