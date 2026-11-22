# Currency Converter

Spring Boot (Java 21) REST API that converts an amount between currencies using
EUR-based exchange rates from `src/main/resources/rates.json`.

## Run

```bash
mvn spring-boot:run      # starts on http://localhost:8080
mvn test                 # runs unit, web-layer and end-to-end tests
```

## API

`POST /api/conversions`

```bash
curl -X POST http://localhost:8080/api/conversions \
  -H "Content-Type: application/json" \
  -d '{"sourceCurrency":"EUR","targetCurrency":"USD","amount":100}'
```

```json
{ "convertedAmount": 116.06 }
```

Errors are returned as RFC 7807 `application/problem+json` with status `400` for:
a missing/blank currency, an `amount` that is missing, zero or negative, an unknown
currency code, or a malformed body.

Also available: Swagger UI at `/swagger-ui.html` and health at `/actuator/health`.

## Design decisions and trade-offs

- **Cross rates via EUR**: `result = amount * rate(target) / rate(source)`, so any pair in the
  rates file works. `EUR` itself is implicitly supported with rate 1.
- **`BigDecimal` everywhere**, never `double`. Multiplication happens first and division last, so the
  result is rounded exactly once, to 2 decimals with `HALF_UP`.
- **No database**: the rates are a static, read-only reference file, so they are loaded once at
  startup into an immutable in-memory map. The file is validated and the app fails fast if it is
  missing or malformed.
- **`ExchangeRateProvider` interface** is the seam for a DB or a live rates API with caching and
  periodic refresh, which a real billing system would need.
- Rates are static: no refresh, no rate history or timestamps, no auth or rate limiting.
- All currencies are rounded to 2 decimals; currencies with other minor units (e.g. JPY) are not
  handled specially.
