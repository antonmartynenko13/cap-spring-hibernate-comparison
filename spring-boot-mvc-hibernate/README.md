# Spring Boot MVC Hibernate – User/Department API

A Spring Boot 3 application demonstrating a REST/JSON CRUD API using Spring MVC, Spring Data JPA (Hibernate), Spring Security (HTTP Basic), and an H2 in-memory database.

This project is paired with [../sap-cap-java](../sap-cap-java/README.md), which implements identical functionality using the SAP CAP Java framework. See the [root README](../README.md) for a side-by-side comparison.

---

## Prerequisites

- Java 21
- Maven 3.8+

---

## Build

```bash
mvn clean package
```

## Run

```bash
mvn spring-boot:run
```

Or, after building:

```bash
java -jar target/spring-demo-1.0.0-SNAPSHOT.jar
```

The application starts on **http://localhost:8080**.

---

## Credentials

| Username | Password | Role | Permissions |
|---|---|---|---|
| `admin1` | `admin1pass` | `ADMIN` | Full CRUD + custom operations |
| `user1` | `user1pass` | `USER` | Read-only |

---

## Endpoints

### Users

| Method | Path | Role | Description |
|--------|------|------|-------------|
| `GET` | `/api/users` | USER, ADMIN | List all users |
| `GET` | `/api/users/{id}` | USER, ADMIN | Get user by ID |
| `GET` | `/api/users/count` | USER, ADMIN | **OData Function equivalent** – returns total user count |
| `POST` | `/api/users` | ADMIN | Create a user |
| `PUT` | `/api/users/{id}` | ADMIN | Update a user |
| `DELETE` | `/api/users/{id}` | ADMIN | Delete a user |
| `POST` | `/api/users/{id}/assign-department` | ADMIN | **OData Action equivalent** – assign user to a department |

### Departments

| Method | Path | Role | Description |
|--------|------|------|-------------|
| `GET` | `/api/departments` | USER, ADMIN | List all departments |
| `GET` | `/api/departments/{id}` | USER, ADMIN | Get department by ID |
| `POST` | `/api/departments` | ADMIN | Create a department |
| `PUT` | `/api/departments/{id}` | ADMIN | Update a department |
| `DELETE` | `/api/departments/{id}` | ADMIN | Delete a department |

---

## Request / Response Examples

**Create User** `POST /api/users`
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "country": "Germany",
  "city": "Berlin"
}
```

**Assign Department** `POST /api/users/{id}/assign-department`
```json
{
  "departmentId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Note:** Address fields (`country`, `city`) are flat in this API. In the CAP project they are nested inside an `address` object – this difference is a direct consequence of how each framework serialises an embedded/structured type.

---

## H2 Console

Available at **http://localhost:8080/h2-console**

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:demodb` |
| Username | `sa` |
| Password | *(empty)* |

---

## Key Files

| Concept | File |
|---|---|
| Security configuration | [`config/SecurityConfiguration.java`](src/main/java/com/example/springdemo/config/SecurityConfiguration.java) |
| User entity (with embedded Address and many-to-many) | [`entity/User.java`](src/main/java/com/example/springdemo/entity/User.java) |
| Department entity | [`entity/Department.java`](src/main/java/com/example/springdemo/entity/Department.java) |
| Embedded address type | [`entity/Address.java`](src/main/java/com/example/springdemo/entity/Address.java) |
| User CRUD + count + assign-department | [`controller/UserController.java`](src/main/java/com/example/springdemo/controller/UserController.java) |
| Department CRUD | [`controller/DepartmentController.java`](src/main/java/com/example/springdemo/controller/DepartmentController.java) |
| Business logic + DTO mapping | [`service/UserService.java`](src/main/java/com/example/springdemo/service/UserService.java) |
