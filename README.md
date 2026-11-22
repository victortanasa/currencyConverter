# Currency Converter

A simple Spring Boot application that converts amounts between a fixed set
of currencies using an in-memory exchange rate table (relative to USD).

## Run

```bash
mvn spring-boot:run
```

## Example request

```bash
curl -X POST http://localhost:8080/api/conversions \
  -H "Content-Type: application/json" \
  -d '{"fromCurrency":"USD","toCurrency":"EUR","amount":100}'
```
