# AuditFlow Backend

Backend API for AuditFlow, a security audit management platform.

## Tech Stack

- Java 17
- Spring Boot
- Maven
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- Docker Compose
- JWT
- HTTPOnly Cookie Authentication
- Testcontainers
- JUnit 5

## Main Features

- User authentication.
- Register.
- Login.
- Logout.
- Authenticated user endpoint.
- JWT stored in HTTPOnly cookie.
- Role-based access control.
- Multi-tenant organization isolation.
- Project CRUD.
- Auditor assignment to projects.
- Findings CRUD.
- Findings filters.
- Findings pagination.
- IDOR protection.
- Backend authorization tests.

## Roles

AuditFlow supports two roles:

| Role | Description |
|---|---|
| ADMIN | Can manage projects, assign auditors and access organization data. |
| AUDITOR | Can only access assigned projects and findings in assigned projects. |

## Test Credentials

Seed data creates the following users:

| Role | Email | Password |
|---|---|---|
| ADMIN | admin@auditflow.local | Admin123! |
| AUDITOR | auditor@auditflow.local | Auditor123! |

## Local URLs

| Service | URL |
|---|---|
| Backend API | http://localhost:8080 |
| PostgreSQL | localhost:15432 |
| Frontend | http://localhost:3000 |

## Deploy URLs

Deployment URLs are pending.

| Service | URL |
|---|---|
| Backend production | Pending |
| Frontend production | Pending |
| Database production | Pending |

## Environment Variables

Local configuration is currently stored in `src/main/resources/application.yml`.

Important values:

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:15432/auditflow_db
    username: auditflow_user
    password: auditflow_password

app:
  security:
    jwt:
      secret: auditflow-dev-secret-key-minimum-32-characters-123456
      expiration-ms: 86400000
    cookie:
      name: access_token
      secure: false
      same-site: Lax
      max-age-seconds: 86400
    cors:
      allowed-origins:
        - http://localhost:3000
```

For production:

- Use a strong `JWT_SECRET`.
- Set cookie `secure=true`.
- Use HTTPS.
- Restrict CORS to the production frontend URL.
- Do not expose real secrets in Git.

## Database

PostgreSQL is managed with Docker Compose.

Start database:

```bash
docker compose up -d
```

Check container:

```bash
docker ps
```

Expected container:

```txt
auditflow_postgres
```

Database connection:

```txt
Host: localhost
Port: 15432
Database: auditflow_db
User: auditflow_user
Password: auditflow_password
```

## Run Locally

Start PostgreSQL:

```bash
docker compose up -d
```

Run backend:

```bash
mvn spring-boot:run
```

Backend should start at:

```txt
http://localhost:8080
```

Health check:

```bash
curl.exe http://localhost:8080/api/health
```

Expected response:

```json
{
  "service": "auditflow-backend",
  "status": "UP"
}
```

## Run Tests

Run backend tests:

```bash
mvn clean test
```

Run full build:

```bash
mvn clean install
```

## Authentication Flow

AuditFlow uses JWT stored in an HTTPOnly cookie.

Rules:

- The token is not returned in JSON.
- The token is not stored in LocalStorage.
- The token is not stored in SessionStorage.
- The frontend sends requests using `credentials: "include"`.
- The backend validates the cookie on protected endpoints.

## API Overview

### Auth

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register public user as AUDITOR |
| POST | `/api/auth/login` | Login and set HTTPOnly cookie |
| POST | `/api/auth/logout` | Clear auth cookie |
| GET | `/api/auth/me` | Get authenticated user |

### Projects

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/projects` | Create project, ADMIN only |
| GET | `/api/projects` | List projects visible to current user |
| GET | `/api/projects/{id}` | Get project by ID |
| PUT | `/api/projects/{id}` | Update project, ADMIN only |
| DELETE | `/api/projects/{id}` | Delete project, ADMIN only |
| POST | `/api/projects/{id}/auditors` | Assign auditor, ADMIN only |

### Findings

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/findings` | Create finding |
| GET | `/api/findings` | List findings with filters and pagination |
| GET | `/api/findings/{id}` | Get finding by ID |
| PUT | `/api/findings/{id}` | Update finding |
| DELETE | `/api/findings/{id}` | Delete finding |

Finding filters:

```txt
/api/findings?severity=HIGH&status=OPEN&page=0&size=10
```

Valid severities:

```txt
LOW
MEDIUM
HIGH
CRITICAL
```

Valid statuses:

```txt
OPEN
IN_PROGRESS
RESOLVED
CLOSED
FALSE_POSITIVE
```

## Security Rules

### 401 Unauthorized

Returned when:

- No valid authentication cookie exists.
- JWT is missing.
- JWT is invalid.
- JWT is expired.

### 403 Forbidden

Returned when:

- User is authenticated but does not have the required role.
- Auditor tries to access a project not assigned to them.
- Auditor tries to create a finding in a project not assigned to them.

### 404 Not Found

Returned when:

- Resource does not exist.
- Resource belongs to another organization.

This helps avoid leaking resource existence across organizations.

## Documentation

Additional documentation:

```txt
AI-WORKFLOW.md
docs/database-diagram.md
docs/access-control.md
docs/backend-test-checklist.md
```

## Technical Decisions

- Spring Boot was used for a structured backend API.
- PostgreSQL was used for relational data and multi-tenant relationships.
- JWT is stored in HTTPOnly cookies instead of frontend storage.
- RBAC is enforced in backend services.
- Organization-based queries are used to avoid IDOR.
- Testcontainers is used for backend integration tests.