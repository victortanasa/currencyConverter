# Currency Converter

Spring Boot (Java 21, Maven) REST API that converts an amount between currencies using the exchange
rates in `src/main/resources/rates.json`.

## Run

Requirements: JDK 21+ and Maven 3.9+.

```bash
mvn spring-boot:run     # starts on http://localhost:8080
mvn test                # runs the tests
```

Swagger UI: http://localhost:8080/swagger-ui.html

## Endpoints

### Convert: `POST /api/conversions`

```bash
curl -X POST 'http://localhost:8080/api/conversions' \
  -H 'Content-Type: application/json' \
  -d '{"sourceCurrency":"EUR","targetCurrency":"USD","amount":100}'
```

Payload: `sourceCurrency` and `targetCurrency` (ISO codes, case-insensitive) and `amount`
(greater than 0, up to 12 integer and 2 fraction digits).

Response `200`: `{"convertedAmount": 116.06}`

### Errors

Invalid input (blank or missing field, bad amount, unknown currency, malformed JSON) returns
`400` as `application/problem+json`, for example:

```json
{ "status": 400, "title": "Bad Request", "detail": "Validation failed",
  "errors": ["sourceCurrency: must not be blank"] }
```
