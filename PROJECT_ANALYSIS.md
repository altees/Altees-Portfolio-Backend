# Project Analysis — Altees Portfolio Backend

> **Date:** 2026-07-02
> **Stack:** Spring Boot 4.1.0 · Java 17 · Spring Data JPA · PostgreSQL · Lombok · Bean Validation
> **Scope:** Full codebase review — architecture, patterns, strengths, weaknesses, code smells, and improvement recommendations.
> **Note:** This is a read-only analysis. No files were modified.

---

## 1. Overall Architecture

This is a **RESTful backend API** for a personal developer portfolio website. It exposes CRUD endpoints for every portfolio section (profile, skills, education, work experience, certifications, projects, social links) and a single **aggregate read endpoint** (`GET /api/v1/portfolio`) that assembles the full portfolio in one response.

### Architectural Style

```
Client (Frontend / Browser)
        │
        ▼
  REST API  (Spring MVC)
        │
   Controller Layer       — routes HTTP to service, handles request/response shaping
        │
   Service Layer          — business logic, transaction management, DTO mapping
        │
   Repository Layer       — Spring Data JPA interfaces, DB access
        │
   Entity Layer           — JPA-mapped domain objects
        │
  PostgreSQL (localhost:5432 / portfolio_db)
```

### Request / Response Envelope

Every response is standardised:

- **Success** → `ApiResponse<T>` `{ success, message, data, timestamp }`
- **Error** → `ApiError` `{ status, error, message, fieldErrors, timestamp }`

Validation errors include a `fieldErrors` map (`field → message`). All other errors omit it.

### Notable Design Decision — Singleton Profile

The system enforces that only **one** `Profile` row ever exists. This is a deliberate portfolio-site constraint: there is one owner, one resume, one identity. Creation is a `POST /api/v1/profile` that rejects a second create with 409. Update is an ID-less `PUT /api/v1/profile` that always targets the single existing row. All other resources (skills, projects, etc.) are unrestricted collections.

### Aggregate Endpoint

`GET /api/v1/portfolio` is the primary public-facing endpoint. `PortfolioService` calls all six domain services sequentially and returns a single `PortfolioResponse`. This is the endpoint a frontend SPA or SSG site would call on page load to hydrate the entire portfolio in one round-trip.

---

## 2. Package Structure

The project uses **domain-first, then layer** packaging — the right choice at this scale.

```
com.altees.portfolio
│
├── common/
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java      @RestControllerAdvice
│   │   ├── ResourceNotFoundException.java   → HTTP 404
│   │   └── DuplicateResourceException.java  → HTTP 409
│   └── response/
│       ├── ApiResponse.java                 Generic success envelope (record)
│       └── ApiError.java                    Generic error envelope (record)
│
├── profile/
│   ├── controller/   ProfileController
│   ├── service/      ProfileService
│   ├── repository/   ProfileRepository
│   ├── entity/       Profile
│   └── dto/          ProfileRequest, ProfileResponse
│
├── sociallink/
│   ├── controller/   SocialLinkController
│   ├── service/      SocialLinkService
│   ├── repository/   SocialLinkRepository
│   ├── entity/       SocialLink, SocialPlatform (enum)
│   └── dto/          SocialLinkRequest, SocialLinkResponse
│
├── skill/
│   ├── controller/   SkillController
│   ├── service/      SkillService
│   ├── repository/   SkillRepository
│   ├── entity/       Skill, SkillCategory (enum), ProficiencyLevel (enum)
│   └── dto/          SkillRequest, SkillResponse
│
├── education/
│   ├── controller/   EducationController
│   ├── service/      EducationService
│   ├── repository/   EducationRepository
│   ├── entity/       Education
│   └── dto/          EducationRequest, EducationResponse
│
├── experience/
│   ├── controller/   WorkExperienceController
│   ├── service/      WorkExperienceService
│   ├── repository/   WorkExperienceRepository
│   ├── entity/       WorkExperience
│   └── dto/          WorkExperienceRequest, WorkExperienceResponse
│
├── certification/
│   ├── controller/   CertificationController
│   ├── service/      CertificationService
│   ├── repository/   CertificationRepository
│   ├── entity/       Certification
│   └── dto/          CertificationRequest, CertificationResponse
│
├── project/
│   ├── controller/   ProjectController
│   ├── service/      ProjectService
│   ├── repository/   ProjectRepository
│   ├── entity/       Project
│   └── dto/          ProjectRequest, ProjectResponse
│
└── portfolio/
    ├── controller/   PortfolioController
    ├── service/      PortfolioService
    └── dto/          PortfolioResponse
                      (no entity or repository — pure aggregation)
```

### Observations

- Every domain module follows the **exact same internal layout** (`controller/`, `service/`, `repository/`, `entity/`, `dto/`), making navigation predictable.
- `portfolio` is intentionally shallow — it has no entity or repository because it is a read-only aggregation of other modules, not an independent domain concept.
- `common` cleanly separates cross-cutting infrastructure from domain logic.
- The one violation of module isolation: `SocialLinkService` directly injects `ProfileRepository`, creating a **cross-domain repository dependency** (discussed in §7).

---

## 3. Design Patterns in Use

### 3.1 Repository Pattern
Each domain entity has a corresponding Spring Data `JpaRepository<Entity, Long>` interface. Data access is fully abstracted behind these interfaces. Services never interact with `EntityManager` directly.

### 3.2 Service Layer Pattern
All business logic lives in `@Service` classes. Controllers are thin — they handle HTTP routing and delegate immediately to the service. Services handle validation, entity manipulation, and DTO mapping.

```
Controller → receives HTTP, calls service, returns ResponseEntity
Service    → owns business logic, transaction boundary, mapping
Repository → owns DB access
```

### 3.3 Data Transfer Object (DTO) Pattern
Every domain has separate Request and Response DTOs. Entities are never exposed directly over the wire. This decouples the HTTP contract from the persistence model.

All DTOs are implemented as **Java records**, which is idiomatic Java 17 for immutable value carriers.

```java
public record ProfileRequest(
    @NotBlank String name,
    @NotBlank @Email String email,
    String phone, ...
) {}
```

### 3.4 Template Method Pattern — `applyRequest()`
Every service that supports create and update uses a private `applyRequest(Entity entity, Request request)` method that maps fields from the DTO onto the entity. This method is called from both `create()` and `update()`, avoiding duplicated field-mapping code.

```java
public SkillResponse create(SkillRequest request) {
    Skill skill = new Skill();
    applyRequest(skill, request);      // shared mapping
    return toResponse(skillRepository.save(skill));
}

public SkillResponse update(Long id, SkillRequest request) {
    Skill skill = findOrThrow(id);
    applyRequest(skill, request);      // same mapping reused
    return toResponse(skillRepository.save(skill));
}
```

### 3.5 Aggregate / Facade Pattern — `PortfolioService`
`PortfolioService` acts as a **facade** over the six individual domain services. Its single `getPortfolio()` method hides the complexity of calling six services and assembling one composite response.

```java
public PortfolioResponse getPortfolio() {
    return new PortfolioResponse(
        profileService.findProfile(),
        skillService.findAll(null),
        educationService.findAll(),
        workExperienceService.findAll(),
        certificationService.findAll(),
        projectService.findAll(null)
    );
}
```

### 3.6 Global Exception Handling — Interceptor / Handler Pattern
`GlobalExceptionHandler` uses `@RestControllerAdvice` to centrally intercept exceptions from all controllers. Rather than each controller handling its own errors, exception mapping is defined once.

### 3.7 Value Object Pattern — Enums
`SocialPlatform`, `SkillCategory`, and `ProficiencyLevel` are stored as `EnumType.STRING` in the database, making them readable, migration-safe (no implicit ordinal ordering), and self-documenting in the schema.

### 3.8 Filter via Query Parameter
`SkillController` accepts `?category=BACKEND` and `ProjectController` accepts `?featured=true`. Services branch on null vs non-null to return filtered or full lists:

```java
List<Skill> skills = (category != null)
    ? skillRepository.findAllByCategory(category)
    : skillRepository.findAll();
```

---

## 4. Strengths

### 4.1 Consistent, Predictable Structure
Every module follows the same layout. A developer can navigate to any feature's controller, service, or entity by following the same mental model every time. No surprises.

### 4.2 Constructor Injection Throughout
Zero field injection (`@Autowired` on fields). Every dependency is declared `final` and injected via constructor. This makes beans immutable, testable without Spring, and ensures the dependency graph is explicit at construction time.

### 4.3 Java Records for DTOs
Using `record` for all request/response types is the right Java 17 choice. Records are immutable, concise, and have auto-generated `equals`, `hashCode`, and `toString`. No boilerplate getters, no risk of accidentally mutating a DTO mid-request.

### 4.4 Proper `@Transactional` Discipline
- Class-level `@Transactional` on services ensures write methods are always in a transaction.
- `@Transactional(readOnly = true)` on read methods skips Hibernate dirty checking, sets flush mode to `MANUAL`, and signals to the connection pool that reads can be routed to a replica.
- `PortfolioService`'s class-level `@Transactional(readOnly = true)` means all six service calls inside `getPortfolio()` share **one session and one connection**, reducing pool pressure and enabling first-level cache sharing.

### 4.5 Standardised API Envelope
`ApiResponse<T>` and `ApiError` create a consistent contract for every endpoint. Clients can always expect the same outer shape regardless of resource type. Validation errors include a `fieldErrors` map pointing directly to the offending field.

### 4.6 Sensible Validation at the Entry Point
Bean Validation annotations (`@NotBlank`, `@NotNull`, `@Email`, `@Min`, `@Max`) are on the DTO record components, not inside the service. This is the correct placement — validation fires at the Spring MVC layer before the service is even called.

### 4.7 Proper Lombok Usage
Lombok is used exclusively on entities (`@Getter`, `@Setter`, `@NoArgsConstructor`, `@ToString`). It is correctly excluded from records (which don't need it) and not overused. `@ToString(exclude = "socialLinks")` on `Profile` prevents infinite recursion when printing a profile that has a bidirectional association.

### 4.8 `EnumType.STRING` for All Enums
All three enums (`SocialPlatform`, `SkillCategory`, `ProficiencyLevel`) are persisted as strings. This is the safe, correct default — ordinal-based persistence breaks silently when enum values are reordered or inserted.

### 4.9 `@OrderColumn` on Element Collections
`WorkExperience.keyAchievements` and `Project.techStack` both use `@OrderColumn(name = "sort_order")`. Insertion order is preserved in the database, so the list returned by the API reflects the order the client submitted. This is easy to overlook and handled correctly here.

### 4.10 `@CreationTimestamp` / `@UpdateTimestamp` on Every Entity
Audit columns are handled automatically by Hibernate on every entity. No service code needs to set these manually. The `createdAt` column is marked `updatable = false`, preventing accidental overwrites.

### 4.11 Production Config Separation
`application-prod.yaml` correctly overrides `ddl-auto: validate` (from `update` in dev), preventing Hibernate from making destructive schema changes in production. SQL logging is also disabled in the prod profile.

---

## 5. Weaknesses

### 5.1 `GlobalExceptionHandler` Does Not Extend `ResponseEntityExceptionHandler`

This is the most impactful structural gap.

Spring MVC's `ExceptionHandlerExceptionResolver` (which processes `@ExceptionHandler` methods) runs at **order 1**, before `DefaultHandlerExceptionResolver` (order `Integer.MAX_VALUE`). This means the catch-all `@ExceptionHandler(Exception.class)` in `GlobalExceptionHandler` intercepts Spring MVC's own built-in exceptions before they can be mapped to their intended HTTP status codes.

| Exception | Intended Status | Actual Status |
|---|---|---|
| `HttpRequestMethodNotAllowedException` | 405 Method Not Allowed | **500** |
| `HttpMediaTypeNotSupportedException` | 415 Unsupported Media Type | **500** |
| `MissingServletRequestParameterException` | 400 Bad Request | **500** |
| `NoResourceFoundException` | 404 Not Found | **500** |
| `HttpMediaTypeNotAcceptableException` | 406 Not Acceptable | **500** |

Fix: extend `ResponseEntityExceptionHandler`, which delegates Spring MVC exceptions to the correct handlers before the catch-all runs.

### 5.2 `handleGeneral()` Swallows Exceptions Silently

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ApiError> handleGeneral(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiError(500, "INTERNAL_SERVER_ERROR",
                    "An unexpected error occurred", null, LocalDateTime.now()));
    // ex is never logged
}
```

The exception is caught, a generic 500 body is returned, and the exception is **discarded without logging**. In production there is no stack trace, no message, and no way to correlate the 500 response to its cause. Every unexpected runtime error becomes a black hole.

### 5.3 `DataIntegrityViolationException` Not Handled

Two DB unique constraints will cause `500` responses instead of the correct `409 Conflict`:

1. `uq_social_link_platform_profile` on `(platform, profile_id)` — duplicate platform per profile on `POST /profile/social-links`
2. `uq_profile_email` on `profile.email` — changing the email to an already-taken value on `PUT /profile`

The service has no pre-insert check, and `DataIntegrityViolationException` has no dedicated `@ExceptionHandler`. It falls through to `handleGeneral()` → 500.

### 5.4 Sensitive Data Logged at TRACE in Production

`application.yaml` sets:
```yaml
logging:
  level:
    org.hibernate.orm.jdbc.bind: TRACE
```

`application-prod.yaml` overrides `org.hibernate.SQL` to `WARN`, but **never overrides `org.hibernate.orm.jdbc.bind`**. In production, every SQL bind parameter — email addresses, phone numbers, URLs — is written to the log at TRACE level. Spring Boot's default log output includes TRACE if any appender is configured to receive it.

### 5.5 No Cross-Field Validation on DTOs

`WorkExperienceRequest` has no constraint enforcing:
- `endDate` is after `startDate`
- `current = true` implies `endDate` is null

`EducationRequest` has no constraint enforcing:
- `endYear >= startYear`

Both violations are accepted, persisted, and silently produce logically contradictory data.

### 5.6 TOCTOU Race in All Delete Methods

Every service uses this two-step pattern:
```java
if (!repository.existsById(id)) { throw new ResourceNotFoundException(...); }
repository.deleteById(id);
```

Between `existsById` and `deleteById`, another thread can delete the same record. `deleteById` then either silently no-ops (Spring Data JPA 3.x) or throws `EmptyResultDataAccessException` — neither of which is caught or mapped, resulting in no exception thrown or a 500. Additionally, Spring Data JPA's `deleteById` internally calls `findById` anyway, making this pattern **three queries** (existsById + findById + DELETE) when two suffice.

### 5.7 Singleton Profile Race Condition

```java
if (profileRepository.count() > 0) {
    throw new DuplicateResourceException("Profile already exists...");
}
// ...
profileRepository.save(profile);
```

Under PostgreSQL's default `READ COMMITTED` isolation, two concurrent `POST /profile` requests both read `count() = 0`, both pass the guard, and both attempt `save()`. If the emails differ, two profiles are committed. The singleton invariant is broken with no DB-level enforcement (no unique sentinel column, no partial index limiting rows to one).

### 5.8 `profileRepository.findAll()` Used as a Single-Row Lookup

Both `ProfileService` (twice) and `SocialLinkService` call:
```java
profileRepository.findAll().stream().findFirst()
```

This issues `SELECT * FROM profiles` with **no LIMIT clause**, fetching every row into memory before discarding all but the first. There is no repository method that pushes the `LIMIT 1` to the database.

### 5.9 No Security / Authentication Layer

All endpoints — including write operations (`POST`, `PUT`, `DELETE`) — are publicly accessible with no authentication. Any anonymous caller can modify the portfolio, add fake skills, delete certifications, or overwrite the profile. For a personal portfolio with a public read API this may be intentional, but it is a significant gap if the admin operations are meant to be protected.

### 5.10 `ddl-auto: update` in Default (Dev) Profile

Using `update` means Hibernate modifies the schema automatically on startup. While convenient in early development, `update` can:
- Silently drop columns it thinks are no longer needed (in some Hibernate versions)
- Never remove columns or tables (so stale columns accumulate)
- Produce non-repeatable schema state across environments

A migration tool (`Flyway` or `Liquibase`) is the correct long-term replacement.

---

## 6. Potential Improvements

### 6.1 Extend `ResponseEntityExceptionHandler`
```java
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    // existing handlers remain
    // Spring MVC exceptions now route to their correct 4xx statuses
}
```
Impact: 405, 415, 400, 404 responses work correctly for framework-level errors.

### 6.2 Log All Unhandled Exceptions
```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ApiError> handleGeneral(Exception ex) {
    log.error("Unhandled exception", ex);   // add this line
    return ResponseEntity.status(INTERNAL_SERVER_ERROR)
            .body(new ApiError(500, "INTERNAL_SERVER_ERROR",
                    "An unexpected error occurred", null, LocalDateTime.now()));
}
```

### 6.3 Handle `DataIntegrityViolationException`
Add a handler that maps known constraint violations to 409:
```java
@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
    return ResponseEntity.status(CONFLICT)
            .body(new ApiError(409, "CONFLICT",
                    "A resource with these values already exists.", null, LocalDateTime.now()));
}
```

### 6.4 Suppress Bind-Parameter Logging in Production
Add to `application-prod.yaml`:
```yaml
logging:
  level:
    org.hibernate.orm.jdbc.bind: WARN
```

### 6.5 Add Cross-Field Validation to DTOs
Use a class-level `@AssertTrue` method on the record or a custom constraint annotation:
```java
public record WorkExperienceRequest(...) {
    @AssertTrue(message = "End date must be after start date")
    private boolean isEndDateValid() {
        return endDate == null || !endDate.isBefore(startDate);
    }

    @AssertTrue(message = "Current position must not have an end date")
    private boolean isCurrentConsistent() {
        return !current || endDate == null;
    }
}
```

### 6.6 Fix Delete Pattern — Single Query, No Race
Replace the two-step existsById + deleteById with:
```java
public void delete(Long id) {
    Skill skill = skillRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Skill", id));
    skillRepository.delete(skill);
}
```
This is 2 queries (SELECT + DELETE) instead of 3, and has no TOCTOU window.

### 6.7 Add `findFirstByOrderByIdAsc()` to `ProfileRepository`
```java
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findFirstByOrderByIdAsc();
}
```
Replace all three call sites of `findAll().stream().findFirst()` with this. Pushes `LIMIT 1` to the database.

### 6.8 Add Spring Security for Write Endpoints
Protect `POST`, `PUT`, `DELETE` endpoints with HTTP Basic or JWT authentication. Allow unauthenticated `GET` requests (the public portfolio reads). Even a single static API key passed as a header would be better than open write access.

### 6.9 Introduce Flyway or Liquibase
Replace `ddl-auto: update` with `ddl-auto: validate` in dev and manage schema via versioned migration scripts. This gives repeatable, auditable schema evolution across dev, staging, and production.

### 6.10 Enforce DB-Level Singleton Profile
Add a partial unique index or a check constraint to guarantee the `profiles` table never has more than one row, independent of application logic:
```sql
CREATE UNIQUE INDEX uq_profiles_singleton ON profiles ((true));
```
This makes the application-level `count()` guard redundant and eliminates the concurrency race.

---

## 7. Code Smells

### 7.1 `toResponse()` is Public on Service Classes
`ProfileService.toResponse()` and `SocialLinkService.toResponse()` are declared `public`. This means:
- They can be called from anywhere, including on **detached entities** outside a transaction, causing `LazyInitializationException`
- They leak a mapping concern through the service's public API
- They invite callers to bypass the service's query methods entirely

These methods should be `private`. DTO mapping is an internal concern of the service.

### 7.2 Three Copies of the Same "Find First Profile" Logic

Identical pattern in three places:
```java
profileRepository.findAll().stream().findFirst().orElseThrow(...)
```
- `ProfileService.findProfile()` (line 27)
- `ProfileService.updateProfile()` (line 44)
- `SocialLinkService.resolveProfile()` (line 59)

This is a violation of DRY and hides the fact that all three are doing the same thing. A single repository method (`findFirstByOrderByIdAsc()`) replaces all three.

### 7.3 `SocialLinkService` Directly Injects `ProfileRepository`

`SocialLinkService` injects `ProfileRepository` solely for `resolveProfile()`. This creates a **cross-domain repository dependency**: the `sociallink` module reaches into the `profile` module's data access layer. If `ProfileService` later adds caching, authorization checks, or business rules to profile access, `SocialLinkService` silently bypasses all of them.

The fix is to inject `ProfileService` instead, and expose a package-accessible method that returns the `Profile` entity (or use an internal service call).

### 7.4 Six Identical Delete Implementations

The same six-line pattern is copy-pasted verbatim across every service:
```java
if (!repository.existsById(id)) {
    throw new ResourceNotFoundException("Entity", id);
}
repository.deleteById(id);
```
This appears in `SkillService`, `EducationService`, `WorkExperienceService`, `CertificationService`, `ProjectService`, and `SocialLinkService`. A shared private helper or a corrected single-query pattern would eliminate all six copies.

### 7.5 `applyRequest()` and `toResponse()` Repeated in Every Service

Every service has a private `applyRequest(Entity, Request)` and a public `toResponse(Entity)`. The shape is identical across all six CRUD services — only the field names change. While duplication at this level is acceptable (it is genuinely different data), it is worth noting as a pattern that would benefit from a mapper library (`MapStruct`) as the project grows, to remove manual field-by-field mapping.

### 7.6 `ApiResponse.ok(T data)` Does Not Delegate to `ok(String message, T data)`

Two factory methods build an `ApiResponse` independently:
```java
public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(true, "Success", data, LocalDateTime.now());
}

public static <T> ApiResponse<T> ok(String message, T data) {
    return new ApiResponse<>(true, message, data, LocalDateTime.now());
}
```
The first should delegate to the second (`return ok("Success", data)`). As written, any change to the construction logic (e.g., switching to a fixed clock for testability) must be made in two places.

### 7.7 Architecture Smell — `PortfolioService` Calls Six Services Sequentially

`getPortfolio()` runs six fully independent DB reads one after another on the same thread. The response time is the **sum** of all six query latencies. Since this is the primary public endpoint, any single slow query blocks the entire response. The reads could be parallelised with `CompletableFuture.allOf(...)`, reducing latency to the **maximum** of the six query times.

### 7.8 `boolean current` as Primitive in `WorkExperienceRequest`

```java
boolean current,   // primitive
```

A primitive `boolean` cannot be null, but Jackson will attempt to deserialize the JSON value `null` into it and throw `HttpMessageNotReadableException`. That exception is not mapped to a 400, so it falls to `handleGeneral()` and returns 500. Using `Boolean` (boxed) with `@NotNull` makes the constraint explicit and the error message actionable.

---

## Summary Table

| Category | Item | Severity |
|---|---|---|
| **Bug** | `GlobalExceptionHandler` swallows Spring MVC exceptions as 500 | High |
| **Bug** | Exceptions in `handleGeneral()` are never logged | High |
| **Bug** | `DataIntegrityViolationException` maps to 500 instead of 409 | High |
| **Security** | SQL bind parameters logged at TRACE in production | High |
| **Security** | No authentication on write endpoints | High |
| **Bug** | `boolean current` primitive causes 500 on null JSON input | Medium |
| **Bug** | `current=true` with non-null `endDate` stored without error | Medium |
| **Bug** | `endYear < startYear` accepted without error | Medium |
| **Bug** | Singleton profile race — two profiles can be created concurrently | Medium |
| **Bug** | Delete TOCTOU — three queries, race window, wrong outcome on concurrent delete | Medium |
| **Performance** | `findAll()` with no LIMIT used for singleton profile lookup | Low |
| **Performance** | `PortfolioService` calls six services sequentially | Low |
| **Performance** | Lazy `socialLinks` load causes N+1 query per profile read | Low |
| **Code Smell** | `toResponse()` is public on service classes | Low |
| **Code Smell** | `SocialLinkService` directly injects `ProfileRepository` | Low |
| **Code Smell** | Delete pattern copy-pasted across six services | Low |
| **Code Smell** | Three copies of `findAll().stream().findFirst()` | Low |
| **Code Smell** | `ApiResponse.ok(T)` duplicates construction instead of delegating | Low |
| **Config** | `ddl-auto: update` in dev; no migration tool | Low |
