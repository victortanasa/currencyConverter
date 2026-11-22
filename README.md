# Currency Converter

Spring Boot (Java 21, Maven) REST API that converts an amount between currencies using exchange
rates loaded from `src/main/resources/rates.json`. Rates can be updated at runtime through an API.

## Getting started

Requirements: JDK 21+ and Maven 3.9+.

```bash
mvn spring-boot:run          # starts on http://localhost:8080
mvn test                     # unit, web-layer and end-to-end tests
mvn package                  # builds target/*.jar, run with: java -jar target/<name>.jar
```

Change the port with `--server.port=8081` (or `SERVER_PORT=8081`).

Also available once running:

| URL | Purpose |
|-----|---------|
| `/swagger-ui.html` | Interactive API docs (OpenAPI) |
| `/v3/api-docs` | OpenAPI spec |
| `/actuator/health` | Health check |

## Endpoints

### `POST /api/conversions` — convert an amount

Request body:

| Field | Type | Rules |
|-------|------|-------|
| `sourceCurrency` | string | required, ISO 4217 code, case-insensitive |
| `targetCurrency` | string | required, ISO 4217 code, case-insensitive |
| `amount` | number | required, greater than 0, at most 12 integer and 2 fraction digits |

Response `200`:

```json
{ "convertedAmount": 116.06 }
```

### `PUT /api/exchange-rates/{sourceCurrency}/{targetCurrency}` — update a rate

Replaces the direct rate of one pair and evicts that pair from the cache. Both currencies must be
known (present in `rates.json`) and different.

Request body:

| Field | Type | Rules |
|-------|------|-------|
| `rate` | number | required, greater than 0, at most 6 integer and 8 fraction digits |

Response: `204 No Content`.

### Errors

Errors are RFC 7807 `application/problem+json`.

| Status | When |
|--------|------|
| `400` | blank/missing field, non-positive or oversized `amount`/`rate`, unknown currency, identical source and target in an update, malformed JSON |
| `500` | unexpected error (generic message, details only in the logs) |

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Validation failed",
  "instance": "/api/conversions",
  "errors": ["sourceCurrency: must not be blank"]
}
```

## curl examples (Postman: Import → Raw text)

Convert:

```bash
curl -X POST 'http://localhost:8080/api/conversions' \
  -H 'Content-Type: application/json' \
  -d '{"sourceCurrency":"EUR","targetCurrency":"USD","amount":100}'
```

Convert through a derived (cross) rate:

```bash
curl -X POST 'http://localhost:8080/api/conversions' \
  -H 'Content-Type: application/json' \
  -d '{"sourceCurrency":"AUD","targetCurrency":"SEK","amount":250.50}'
```

Update a rate:

```bash
curl -X PUT 'http://localhost:8080/api/exchange-rates/EUR/USD' \
  -H 'Content-Type: application/json' \
  -d '{"rate":1.2}'
```

Typical test flow: convert `EUR` → `USD`, update `EUR/USD`, convert again and compare.

## Configuration

`src/main/resources/application.yml`:

| Property | Default | Meaning |
|----------|---------|---------|
| `server.port` | `8080` | HTTP port |
| `spring.cache.caffeine.spec` | `expireAfterWrite=5m,maximumSize=10000` | Rate cache expiry and size |

## Rates data

`rates.json` is a list of blocks, one per base currency (currently EUR, USD and RON), each with
quotes named `<BASE><TARGET>`, e.g. `EURUSD`. No base currency is hard-coded, so adding one is a
data change. The USD and RON blocks are derived from the EUR block (`USDxxx = EURxxx / EURUSD`,
6 decimals) so all blocks agree. The file is validated at startup and the app fails fast if it is
missing or malformed.

## Design

- **Layers**: `controller` (HTTP, validation, error mapping) → `service` (conversion and rate
  update logic) → `storage` (rate lookup, cache, repository) with `dto`, `exception` and `util`.
- **Rate lookup** (`ExchangeRateResolver`): a direct quote is used when one exists (`USD` → `EUR`).
  Otherwise the rate is derived through the first base, in file order, that quotes both currencies
  (`quote(target) / quote(source)`, `DECIMAL128` precision), so e.g. `AUD` → `SEK` works via EUR.
  Currencies with no common base return a 400.
- **Money**: rates are `BigDecimal`, never `double`. The amount is a JSR-354 `MonetaryAmount`
  (Moneta). The result is `amount * rate`, rounded once with `HALF_UP` to the target currency's
  minor units (2 for EUR/USD, 0 for JPY, 3 for KWD).
- **Caching**: resolved rates are cached with Spring Cache and Caffeine (5 minute TTL). A rate update
  evicts the entry of the updated pair.
- **Storage**: `ExchangeRateRepository` is an interface with an in-memory implementation. A database
  or live rates API can replace it without touching the service layer.

## Known limitations

- Rate updates are **in memory only**: they are lost on restart and not shared between instances.
- An update changes only the given direction. Updating `EUR/USD` does not change `USD/EUR`, and
  derived (cross) rates already cached stay until the 5 minute TTL expires.
- No authentication, rate limiting, rate history or timestamps.
