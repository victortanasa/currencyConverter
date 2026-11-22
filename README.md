# Currency Converter

Spring Boot (Java 21) REST API that converts an amount between currencies using
exchange rates from `src/main/resources/rates.json`.

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

- **Multiple base currencies**: `rates.json` is a list of blocks, one per base currency (currently
  EUR, USD and RON), each with quotes like `EURUSD`. No base currency is hard-coded, so adding one is
  a data change. The EUR block is the assignment's data. The USD and RON blocks are real ECB
  reference rates (via frankfurter.dev, 2026-11-20), so they come from a different date than the EUR
  block and are not exactly consistent with it (e.g. `USDEUR` is not `1 / EURUSD`).
- **Rate lookup**: a direct quote is used when one exists (`USD` → `EUR`). Otherwise the rate is
  derived through the first base, in file order, that quotes both currencies (`rate = quote(target) /
  quote(source)`, `DECIMAL128` precision), so e.g. `AUD` → `SEK` works via EUR. Currencies with no
  common base return a 400.
- **`BigDecimal` everywhere**, never `double`. The result is `amount * rate`, rounded exactly once,
  to 2 decimals with `HALF_UP`.
- **No database**: the rates are a static, read-only reference file, so they are loaded once at
  startup into an immutable in-memory map. The file is validated and the app fails fast if it is
  missing or malformed.
- **`ExchangeRateProvider` interface** is the seam for a DB or a live rates API with caching and
  periodic refresh, which a real billing system would need.
- Rates are static: no refresh, no rate history or timestamps, no auth or rate limiting.
- All currencies are rounded to 2 decimals; currencies with other minor units (e.g. JPY) are not
  handled specially.
