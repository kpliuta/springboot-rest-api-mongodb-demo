# Demo Spring Boot REST API + MongoDB Application

## Technology Stack

- Java 21
- String Boot 3.2.0
- Spring Data MongoDB 3.2.0
- springdoc-openapi 2.2.0 (Swagger + OpenAPI 3.1 documentation generation)
- Lombok 1.18.30
- Testcontainers 1.19.3 (integration testing)
- Gatling 3.9.5 (load testing)
- Maven 3.9.5
- Docker / Docker Compose 3.8

## Domain model

Domain model is represented by Order and Product entities with one-to-many relation.
MongoDB (distributed) transaction management was deliberately omitted in favor of Optimistic Locking to avoid unnecessary delays as write operations are atomic in MongoDB by nature.

## Building and Running

### Run Integration Tests
```bash
./mvnw test
```

### Build and Install to Local Maven Repository
```bash
./mvnw install
```

### Build and Run Docker Compose Configuration (app + MongoDB)
```bash
docker compose up --build
```

### Run Load Test (Docker Compose Configuration should be run first)
Executes 1 min simulation with ramping up user count from 20 to 200 (half of which adding and another half removing products to/from the same order):
```bash
./mvnw gatling:test
```

## Documentation

- OpenAPI 3.1 documentation is accessible by _http://localhost:8080/v3/api-docs_ URL
- Swagger UI is accessible by _http://localhost:8080/swagger-ui_ URL
- Gatling load test report will be generated after the test run and accessible in _target/gatling/orderupdatesimulation-<timestamp>/index.html_

