# SAP CAP Java – User/Department OData v4 API

A SAP CAP (Cloud Application Programming model) Java application demonstrating an OData v4 CRUD API with role-based access control, validations, a custom OData Function, and a custom OData Action.

This project is paired with [../spring-boot-mvc-hibernate](../spring-boot-mvc-hibernate/README.md), which implements identical functionality using a plain Spring Boot / Hibernate stack. See the [root README](../README.md) for a side-by-side comparison.

---

## Prerequisites

- Java 17
- Maven 3.8+
- Internet access on first build (Maven downloads Node.js and `@sap/cds-dk` automatically)

---

## IDE Setup

For the full IDE experience, open `sap-cap-java/` as a separate project in its own window. Opening the repository root is possible but the IDE will not resolve the multi-module Maven layout correctly — most source files will appear unresolved.

After opening `sap-cap-java/` as a project, two additional steps are required:

1. **Run the build once** (`mvn generate-sources` or `mvn clean package`) so the `cds-maven-plugin` generates the Java POJOs into `srv/src/gen/java/`. Without this step those classes do not exist on disk and the IDE will show red import errors everywhere.

2. **Mark the generated-sources directory as a source root.** After the first build, right-click `srv/src/gen/java/` in the project tree and choose:
   - IntelliJ IDEA: **Mark Directory as → Generated Sources Root**
   - VS Code (Extension Pack for Java): the directory should be picked up automatically once it exists; if not, add it to `java.project.sourcePaths` in `.vscode/settings.json`

After these two steps all `cds.gen.*` classes resolve correctly and code navigation works.

---

## Build

```bash
mvn clean package
```

> **First build note:** The `cds-maven-plugin` downloads a local Node.js runtime and `@sap/cds-dk 7.6.0` (the CDS compiler toolchain) into the `srv/` module directory. This can take several minutes. Subsequent builds are fast because the tools are cached.

If you have `@sap/cds-dk` installed globally, skip the download with:

```bash
CDSDK_GLOBAL=true mvn clean package
```

## Run

From the `sap-cap-java/` directory:

```bash
mvn -pl srv -am spring-boot:run
```

Or from within `sap-cap-java/srv/`:

```bash
mvn spring-boot:run
```

The OData service is available at:  
**http://localhost:8080/odata/v4/UserDepartmentService**

---

## Credentials

| Username | Password | Role | Permissions |
|---|---|---|---|
| `admin1` | `admin1pass` | `admin` | Full CRUD + Function + Action |
| `user1` | `user1pass` | `user` | Read-only |

Configured via `cds.security.mock.users` in [`application.yaml`](srv/src/main/resources/application.yaml). Spring Boot equivalent: `InMemoryUserDetailsManager` in `SecurityConfiguration.java`.

---

## OData Endpoints

### Service Root
```
GET http://localhost:8080/odata/v4/UserDepartmentService
```
Returns the OData service document listing all entity sets and operations.

### Users

| Method | URL | Role | Description |
|--------|-----|------|-------------|
| `GET` | `/Users` | user, admin | List all users (`$filter`, `$orderby`, `$expand` supported) |
| `GET` | `/Users(guid'...')` | user, admin | Get user by ID |
| `POST` | `/Users` | admin | Create a user |
| `PATCH` | `/Users(guid'...')` | admin | Update a user (OData uses PATCH, not PUT) |
| `DELETE` | `/Users(guid'...')` | admin | Delete a user |

### Departments

| Method | URL | Role | Description |
|--------|-----|------|-------------|
| `GET` | `/Departments` | user, admin | List all departments |
| `GET` | `/Departments(guid'...')` | user, admin | Get department by ID |
| `POST` | `/Departments` | admin | Create a department |
| `PATCH` | `/Departments(guid'...')` | admin | Update a department |
| `DELETE` | `/Departments(guid'...')` | admin | Delete a department |

### Custom Operations

| Type | Method | URL | Role | Description |
|------|--------|-----|------|-------------|
| **Function** | `GET` | `/getUsersCount()` | user, admin | Returns total user count. Parentheses are mandatory in OData URI syntax. |
| **Action** | `POST` | `/assignUserToDepartment` | admin | Assigns a user to a department. |

---

## Request / Response Examples

**Create User** `POST /odata/v4/UserDepartmentService/Users`
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "address_country": "GERMANY",
  "address_city": "Berlin"
}
```

> **Note:** Address fields are flat (`address_country`, `address_city`) because the CDS compiler flattens structured types to columns. Both this project and the Spring Boot project use flat fields — the field names just differ.
**OData Function – Get Users Count**  
`GET /odata/v4/UserDepartmentService/getUsersCount()`
```json
{ "value": 3 }
```

**OData Action – Assign User to Department**  
`POST /odata/v4/UserDepartmentService/assignUserToDepartment`
```json
{
  "userID": "550e8400-e29b-41d4-a716-446655440001",
  "departmentID": "550e8400-e29b-41d4-a716-446655440002"
}
```
Response:
```json
{
  "success": true,
  "message": "User 550e... assigned to department 550e..."
}
```

---

## Key Concepts for Spring Boot Developers

### CDS Model (`db/data-model.cds`)
Defines entities and types in a declarative DSL. The `cds-maven-plugin` compiles this into:
- `schema-h2.sql` – used by Spring Boot to initialise the H2 schema on startup
- Type-safe Java POJOs under `cds.gen.*` (e.g. `Users`, `Users_`, `Departments_`)

Spring Boot equivalent: `@Entity` and `@Embeddable` classes written by hand.

### Service Definition (`srv/user-department-service.cds`)
Exposes the entities as an OData v4 service. Role-based access is declared with `@restrict` annotations here – no Java security configuration class is needed. Spring Boot equivalent: `SecurityConfiguration.java` + `@PreAuthorize` on controller methods.

### Event Handlers (`handlers/`)
The only Java code you need to write. Handlers intercept the standard CRUD lifecycle at `@Before`, `@On`, or `@After` phase. Spring Boot equivalent: service layer + controller methods.

### Generated POJOs
`cds.gen.userdepartmentservice.*` classes (`Users`, `Users_`, `GetUsersCountContext`, `AssignUserToDepartmentContext`, etc.) are generated by the build. They do not exist in source control and will show as unresolved in the IDE until you run `mvn generate-sources`.

---

## Key Files

| Concept | File |
|---------|------|
| Domain model (entities, Address type) | [`db/data-model.cds`](db/data-model.cds) |
| Service + role restrictions + Function + Action | [`srv/user-department-service.cds`](srv/user-department-service.cds) |
| Mock users (Basic Auth) | [`srv/src/main/resources/application.yaml`](srv/src/main/resources/application.yaml) |
| Validation + Function + Action handlers | [`srv/src/main/java/com/example/capdemo/handlers/UserServiceHandler.java`](srv/src/main/java/com/example/capdemo/handlers/UserServiceHandler.java) |
| Build configuration (CDS plugin lifecycle) | [`srv/pom.xml`](srv/pom.xml) |

---

## Testing

Run commands from the `sap-cap-java/` directory.

### Unit tests (Mockito, no Spring context)

```bash
mvn -pl srv -am test
```

### Integration tests (MockMvc + real H2)

```bash
mvn -pl srv -am test-compile failsafe:integration-test failsafe:verify
```

### All tests

```bash
mvn -pl srv -am verify
```

| Suite | Class | What is covered |
|-------|-------|-----------------|
| Unit | `UserServiceHandlerTest` | Handler logic with mocked `PersistenceService`: validation, Function, Action |
| Integration | `UserServiceIT` | Full OData HTTP stack: CRUD, Function, Action, auth, real H2 |
