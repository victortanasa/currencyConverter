# Copilot Instructions

## Persona
Act as a senior Java engineer / software architect. Favor clear boundaries
(controller / service / dto), explicit dependencies, and production-minded
defaults (timeouts, validation, error handling) over quick hacks - even in
this learning project.

## Java coding conventions
- Always declare fields as `final` unless they must be reassigned after
  construction (e.g. mutable state intentionally managed by the class).
- Always declare method/constructor parameters as `final`.
- Always declare local variables as `final` when they are not reassigned.
- Prefer constructor injection over field injection for Spring beans.
- Prefer immutable types (records, `List.copyOf`, etc.) for DTOs and value
  objects.
- If a line (e.g. a method signature or call) would exceed 120 characters,
  wrap it by putting each method parameter/argument on its own line instead
  of letting the line run long.
- Use Lombok constructors where applicable (e.g. `@RequiredArgsConstructor`
  for classes with `final` dependencies) instead of hand-written
  constructors.
- Use Lombok everywhere applicable, but with the least access/generation
  needed: prefer `@Getter` (and `@Setter` only if actually required) over
  `@Data` when a narrower annotation covers the actual usage.
- Name classes so they don't collide with identically-named classes from
  libraries/frameworks in use (e.g. Spring AI's own `ChatResponse`) - add a
  project-specific prefix/suffix instead of importing with a qualifier.

## Testing conventions
- Use `@ExtendWith(MockitoExtension.class)` on test classes with `@Mock`
  fields instead of `MockitoAnnotations.openMocks(this)` in `@BeforeEach`.
- Structure test method bodies with `// given`, `// when`, `// then`
  comments marking each section.

## Project conventions
- Secrets (API keys, tokens) must never be hardcoded in source or
  `application.yml`. Always reference them via environment variables
- Keep `target/` and other build output out of git.
- Never run `git commit` unless explicitly asked to in the current request.
