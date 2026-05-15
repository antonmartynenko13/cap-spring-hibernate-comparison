# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository Purpose

Training demo comparing two implementations of the same REST API:
- `spring-boot-mvc-hibernate/` – plain Spring Boot 3 / Spring MVC / Hibernate stack
- `sap-cap-java/` – SAP CAP (Cloud Application Programming model) Java / OData v4 stack

Target audience: experienced Spring Boot developers learning SAP CAP.

## Build & Run

### Spring Boot project

```bash
cd spring-boot-mvc-hibernate
mvn clean package          # build
mvn spring-boot:run        # run (http://localhost:8080)
```

### SAP CAP project

```bash
cd sap-cap-java
mvn clean package          # first run downloads Node.js + @sap/cds-dk (takes several minutes)
mvn -pl srv -am spring-boot:run   # run (http://localhost:8080/odata/v4/UserDepartmentService)
```

The `cds-maven-plugin` in `sap-cap-java/srv/pom.xml` drives the CDS build lifecycle: it compiles `.cds` files → generates `schema-h2.sql` and type-safe POJOs under `cds.gen.*` (in `srv/src/gen/java/`, gitignored). These classes do not exist until after the first `mvn generate-sources` or `mvn package`.

## Architecture

### Spring Boot (`spring-boot-mvc-hibernate/`)

Standard layered architecture under `src/main/java/com/example/springdemo/`:

- `entity/` – JPA entities. `Address` is `@Embeddable`; `User` owns the many-to-many with `@JoinTable(name="user_departments")`; `Department` is the non-owning side (`mappedBy`). `@Table(name="users")` on `User` is mandatory because `"user"` is a reserved word in H2.
- `repository/` – Spring Data `JpaRepository` interfaces; no custom queries needed.
- `dto/` – explicit request/response DTOs with `jakarta.validation` annotations (`@NotBlank`, `@Email`). `UserDto` exposes address as flat fields and departments as `Set<UUID>` to avoid recursive serialisation.
- `service/` – `@Transactional` beans. Manual entity↔DTO mapping (no MapStruct). `UserService.assignDepartment()` is the Action equivalent; `UserService.count()` is the Function equivalent.
- `controller/` – `@RestController` beans with `@PreAuthorize` for role checks. In `UserController`, `@GetMapping("/count")` **must be declared before** `@GetMapping("/{id}")` to prevent Spring routing "count" as a UUID.
- `config/SecurityConfiguration` – `InMemoryUserDetailsManager` with `{noop}` passwords, `httpBasic`, `@EnableMethodSecurity`. CSRF disabled. H2 console permitted and frame-options disabled.

### SAP CAP (`sap-cap-java/`)

Multi-module Maven project (root `pom.xml` + `srv/pom.xml`):

- `db/data-model.cds` – domain model. `type Address` (CDS structured type = `@Embeddable`). `UserDepartments` is an explicit join entity (CAP has no `@JoinTable` equivalent).
- `srv/user-department-service.cds` – OData v4 service definition. `@requires`/`@restrict` annotations enforce authentication and roles declaratively; no Java security class is needed. Declares `function getUsersCount()` (OData Function, GET) and `action assignUserToDepartment()` (OData Action, POST).
- `srv/src/main/resources/application.yaml` – `cds.security.mock.users` configures in-memory users (admin1/user1). This is the only security configuration; no `SecurityConfiguration.java` exists in the CAP project.
- `srv/src/main/java/com/example/capdemo/handlers/` – the only Java code. `UserServiceHandler` covers validation (`@Before`), the Function (`@On(GetUsersCountContext.CDS_NAME)`), and the Action (`@On(AssignUserToDepartmentContext.CDS_NAME)`). `DepartmentServiceHandler` handles name validation. Both are `@Component @ServiceName(UserDepartmentService_.CDS_NAME) implements EventHandler`.

### Credentials (both projects)

| User | Password | Role |
|---|---|---|
| `admin1` | `admin1pass` | admin – full CRUD + actions |
| `user1` | `user1pass` | user – read-only |

## Key Intentional Differences Between Projects

| Aspect | Spring Boot | CAP |
|---|---|---|
| Address in JSON | flat fields (`country`, `city`) | flat fields (`address_country`, `address_city`) – CDS flattens structured types |
| Update verb | `PUT` (all fields) | `PATCH` (partial) |
| Custom read op | `GET /api/users/count` | `GET /odata/v4/.../getUsersCount()` (parentheses required) |
| Custom mutate op | `POST /api/users/{id}/assign-department` | `POST /odata/v4/.../assignUserToDepartment` (both IDs in body) |
| Security config | Java class | YAML only |
