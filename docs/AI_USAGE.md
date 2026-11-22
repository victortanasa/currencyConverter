# AI Usage Notes

Tool used: GitHub Copilot (CLI agent), working step by step with me reviewing each change.

## What I used AI for

- Scaffolding the Spring Boot project and the first version of the conversion endpoint.
- Implementing features from my instructions: multi-base rates, cross-rate lookup, Moneta-based
  rounding, Spring Cache with Caffeine.
- Writing unit, web-layer and integration tests.
- Discussing design questions: where caching belongs, `BigDecimal` vs `MonetaryAmount`, endpoint
  naming and HTTP method, whether auth was needed.
- Reviewing the whole project for issues, and drafting the README.

## Where it helped

- Faster setup and boilerplate: validation, `ProblemDetail` error handling, Lombok usage, tests.
- Explaining unfamiliar parts (JSR-354 / Moneta, `@Cacheable` vs a hand-written decorator).
- Catching problems I would likely have missed:
  - `100 EUR -> EUR` returned `1E+2` because Moneta strips trailing zeros (fixed, with a regression test).
  - Unbounded `amount` values caused a 500 (now validated).
  - The USD and RON rate blocks were inconsistent with the EUR block (regenerated from it).

## What I changed or improved

- Rejected or reworked several AI suggestions:
  - Reverted its first `ExchangeRates` refactor and reorganised the packages myself (`storage`,
    `exception`, `util`).
  - Replaced a hand-written caching decorator with Spring's `@Cacheable`.
  - Chose the `ExchangeRateRepository` design and the `ExchangeRateResolver` name.
  - Decided not to add authentication.
  - Had an update-rate endpoint built, then removed it: it would need auth and persistence to be safe,
    which is out of scope. The assignment asks for one conversion endpoint.
- Set project conventions that the AI had to follow (`.github/copilot-instructions.md`): `final`
  everywhere, Lombok, constructor injection, test structure, no secrets in config, no commits by the AI.
- Reviewed and ran everything: tests, manual requests, and a probing review of edge cases.
- Kept the scope in check: the README is short and lists only what is needed to run and call the API.
