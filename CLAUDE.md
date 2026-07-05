# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

On Windows use `mvnw.cmd`; on Linux/macOS use `./mvnw`.

```powershell
# Run the application
mvnw.cmd spring-boot:run

# Build (skip tests)
mvnw.cmd package -DskipTests

# Run all tests
mvnw.cmd test

# Run a single test class
mvnw.cmd test -Dtest=AlteesPortfolioBackendApplicationTests

# Clean build
mvnw.cmd clean package
```

## Environment

Set `DB_PASSWORD` before running:

```powershell
$env:DB_PASSWORD = "your_password"
```

PostgreSQL must be running on `localhost:5432` with database `portfolio_db` and user `postgres`.

Profiles: default (dev), `prod` (`spring.profiles.active=prod`). The prod profile overrides `ddl-auto: validate` and disables SQL logging.

## Stack

- **Spring Boot 4.1.0**, Java 17, Maven
- **Spring Web MVC** (`spring-boot-starter-webmvc`) — REST controllers
- **Spring Data JPA** (`spring-boot-starter-data-jpa`) — `@Entity`, `@Repository`, `JpaRepository`
- **Bean Validation** (`spring-boot-starter-validation`) — `@Valid`, `@NotBlank`, `@NotNull`, etc. on record DTOs
- **PostgreSQL** — configured in `src/main/resources/application.yaml`
- **Lombok** — `@Getter`, `@Setter`, `@NoArgsConstructor`, `@ToString` on entities (annotation processing wired in `pom.xml`)

## Architecture

Main package: `com.altees.portfolio`. Standard Spring Boot layering:

```
controller → service → repository → entity
```

Each domain module is a sub-package with its own `controller/`, `service/`, `repository/`, `entity/`, and `dto/` sub-packages.

### Domain modules

| Module | Base package | REST base path |
|---|---|---|
| Profile | `profile` | `GET/POST/PUT /api/v1/profile` |
| Social Links | `sociallink` | `GET/POST/PUT/DELETE /api/v1/profile/social-links` |
| Skills | `skill` | `GET/POST/PUT/DELETE /api/v1/skills` |
| Education | `education` | `GET/POST/PUT/DELETE /api/v1/education` |
| Work Experience | `experience` | `GET/POST/PUT/DELETE /api/v1/experiences` |
| Certifications | `certification` | `GET/POST/PUT/DELETE /api/v1/certifications` |
| Projects | `project` | `GET/POST/PUT/DELETE /api/v1/projects` |
| Portfolio (aggregate) | `portfolio` | `GET /api/v1/portfolio` |

### Key design decisions

- **Singleton profile**: only one `Profile` row is allowed. `POST /profile` throws 409 if one exists. `PUT /profile` updates it in place (no ID in path).
- **`/api/v1/portfolio`** returns the full portfolio in one response, assembled by `PortfolioService` from all domain services.
- **Social links** are owned by the profile (`@OneToMany(cascade = ALL, orphanRemoval = true)`). They are embedded in `ProfileResponse`.
- **DTOs are Java records** — use `record` for all request/response types. No `@Data` on records.
- **Responses** are always wrapped in `ApiResponse<T>` (success flag, message, data, timestamp). Errors use `ApiError`.

### Common infrastructure

| Class | Location | Purpose |
|---|---|---|
| `ApiResponse<T>` | `common/response` | Standard success envelope |
| `ApiError` | `common/response` | Standard error envelope |
| `ResourceNotFoundException` | `common/exception` | Thrown when entity not found → 404 |
| `DuplicateResourceException` | `common/exception` | Thrown on conflict → 409 |
| `GlobalExceptionHandler` | `common/exception` | `@RestControllerAdvice` mapping exceptions to `ApiError` |

### Enums

- `SocialPlatform`: `GITHUB`, `LINKEDIN`, `TWITTER`, `INSTAGRAM`, `YOUTUBE`, `WEBSITE`, `OTHER`
- `SkillCategory`: `FRONTEND`, `BACKEND`, `DATABASE`, `DEVOPS`, `LANGUAGE`, `TOOL`, `OTHER`
- `ProficiencyLevel`: `BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT`

### Database tables

| Table | Entity |
|---|---|
| `profiles` | `Profile` |
| `social_links` | `SocialLink` — unique on `(platform, profile_id)` |
| `skills` | `Skill` — indexed on `category` |
| `education` | `Education` |
| `work_experiences` | `WorkExperience` |
| `work_experience_achievements` | element collection of `WorkExperience.keyAchievements` |
| `certifications` | `Certification` |
| `projects` | `Project` — indexed on `is_featured` |
| `project_tech_stack` | element collection of `Project.techStack` |

All entities use `@CreationTimestamp` / `@UpdateTimestamp` for audit columns.

## Coding conventions

- Follow standard Spring Boot package structure (controller / service / repository / entity / dto per domain).
- Use `camelCase` for variables and methods, `PascalCase` for classes — IntelliJ IDEA default Java formatting style.
- Use `record` classes for all DTOs and value objects.
- Use constructor injection; whenever possible
- Follow SOLID principles and clean code practices.
- Format generated code according to IntelliJ IDEA Java code formating style.
- Use `@Slf4j` for logging
- Avoid Unnecessary logging, log only when necessary and useful for debugging or monitoring.
- Avoid unnecessary comments,Use comments only when code is not self-explanatory and complex logic is involved.

## Other Instructions
- Keep response concise and focused on the specific code-related task or question.
- Avoid providing explanations or context unless explicitly requested.
- Use less tokens in responses to ensure clarity and brevity.


