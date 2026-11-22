package com.currencyconverter.controller;

import com.currencyconverter.service.UnsupportedCurrencyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UnsupportedCurrencyException.class)
    public ResponseEntity<String> handleUnsupportedCurrency(final UnsupportedCurrencyException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}
