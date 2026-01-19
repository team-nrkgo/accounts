# AI Context & Coding Standards

This document describes the architectural patterns, coding standards, and "rules of the road" for the **Accounts Service**. It is intended to help AI agents (and humans) maintain consistency.

## 1. Reference Implementations (Gold Standard)
> **Note**: These paths refer to "Gold Standard" implementations. If you need to replicate a pattern, check these first (if accessible).
- **Utils**: `C:\Users\nrkka\Documents\java-ref\common\common`
- **Account Flow**: `C:\Users\nrkka\Documents\java-ref\account-backup\account`

## 2. Project Structure
- `controller`: REST endpoints. Return `ApiResponse` wrappers where possible.
- `service`: Business logic. Transactional boundaries.
- `repository`: JPA interfaces.
- `model`: `@Entity` classes mapping specific DB tables.
- `dto`: POJOs for request/response bodies.
- `config`: Spring `@Configuration` classes (Security, etc.).

## 3. Database Rules
- **Naming Convention**: Use `snake_case` for all database columns.
    - Example: `first_name`, `user_email`, `org_id`.
- **Schema Source**: `src/main/resources/db/accounts.sql` is the primary reference for the schema.
    - Mirror any entity changes in this SQL file.

## 4. Coding Standards
### Library Usage
- **Lombok (DEPRECATED FOR THIS ENV)**: Do **NOT** use Lombok. The current environment has issues with annotation processing.
    - Use **Manual** Getters, Setters, and Constructors.
    - Do NOT use `@Data`, `@Builder`, etc.
- **BaseEntity**: All entities MUST extend `BaseEntity` to inherit `created_by`, `modified_by`, `created_time`, `modified_time`.

### API Standards
- **Naming Convention**: JSON payloads MUST use `snake_case` keys.
    - Input: `{ "first_name": "John", "password": "..." }`
    - Output: `{ "success": true, "data": { "created_time": "..." } }`
    - This is enforced globally via `spring.jackson.property-naming-strategy=SNAKE_CASE`.
- **Spring Boot**: Follow standard conventions.
- **Jakarta**: Use `jakarta.*` imports (not `javax.*`) for standardized APIs like Persistence and Validation.

### Code Style
- **Imports**: Use exact imports. **NO wildcards** (e.g., `import java.util.*;` is forbidden).
- **Injection**: Prefer **Constructor Injection** over field injection (`@Autowired` on fields).
- **Null Safety**: Use the project's `NullSafe` utility for checks.

## 5. Key Dependencies
- **Spring Security**: Handles authentication/authorization.
- **Spring Data JPA**: Data access.
- **MySQL Connector**: JDBC driver.
