# AGENTS.md - Optiplant Backend

## Architecture Overview
This is a Spring Boot 4.x authentication backend using layered architecture:
- **Controllers** (`/auth` endpoints) handle HTTP requests
- **Services** contain business logic (e.g., `AuthService` for user registration/login)
- **Repositories** (JPA) manage data access (e.g., `UserRepository.findByUsername()`)
- **Entities** represent database tables (e.g., `User` with username/password/role)
- **DTOs** use records for request/response data transfer with Jakarta validation
- **Security** configured in `SecurityConfig` (permits `/auth/**`, Swagger; requires auth elsewhere)

Data flow: Client → Controller → Service → Repository → Entity → Database (PostgreSQL on Neon)

## Key Patterns & Conventions
- **DTOs**: Records with `@NotBlank` validation, Spanish error messages (e.g., "El usuario es obligatorio")
- **Entities**: Lombok `@Getter/@Setter`, `@AllArgsConstructor` + public no-args constructor
- **Security**: JWT tokens (24h expiration, hardcoded secret), BCrypt password encoding
- **Database**: `ddl-auto=update`, `show-sql=true`, `format_sql=true`, `open-in-view=false`
- **Naming**: `userName` (camelCase) in DTOs, `username` in entities; role defaults to "USER"

## Developer Workflows
- **Run**: `./gradlew bootRun` (starts on port 8080)
- **Build**: `./gradlew build` (includes tests, though currently minimal)
- **Test**: `./gradlew test` (only context loading test exists)
- **Debug**: SQL queries logged to console due to `show-sql=true`
- **API Docs**: Swagger at `http://localhost:8080/swagger-ui/index.html`

## Dependencies & Integrations
- **Database**: PostgreSQL (Neon cloud) - connection in `application.properties`
- **JWT**: `io.jsonwebtoken` for token generation/validation
- **Validation**: Jakarta Bean Validation on DTOs
- **Docs**: SpringDoc OpenAPI for Swagger UI

## Common Tasks
- Add new endpoints: Create DTOs in `/dto`, update Controller/Service/Repository as needed
- Modify auth: Update `SecurityConfig` for new permitted paths
- Database changes: Alter `User` entity, rely on `ddl-auto=update` for schema updates
- Error handling: Currently throws `RuntimeException` (e.g., invalid credentials) - no global handler

## Reference Files
- `src/main/resources/application.properties`: DB config, JPA settings
- `src/main/java/com/optiplant/backend/configuration/security/SecurityConfig.java`: Auth rules
- `src/main/java/com/optiplant/backend/service/AuthService.java`: Registration/login logic
- `src/main/java/com/optiplant/backend/entity/User.java`: User model
- `README.md`: Project overview and setup</content>
<parameter name="filePath">/home/betancourt/Anderson Work/reto-optiplant-backend/AGENTS.md
