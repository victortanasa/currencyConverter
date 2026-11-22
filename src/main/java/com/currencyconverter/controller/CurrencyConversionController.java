package com.currencyconverter.controller;

import com.currencyconverter.dto.ConversionRequest;
import com.currencyconverter.dto.ConversionResponse;
import com.currencyconverter.service.CurrencyConversionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversions")
@RequiredArgsConstructor
public class CurrencyConversionController {

    private final CurrencyConversionService currencyConversionService;

    @PostMapping
    public ConversionResponse convert(@Valid @RequestBody final ConversionRequest request) {
        return currencyConversionService.convert(request);
    }
}
