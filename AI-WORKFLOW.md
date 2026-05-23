# AI Workflow — AuditFlow Backend

## Purpose

This document explains how AI assistance was used during the development of the AuditFlow backend.

AuditFlow was built as a fullstack MVP for managing security audit projects and findings.

The backend focuses on:

- Secure authentication.
- Cookie-based JWT sessions.
- Role-based access control.
- Multi-tenant organization isolation.
- IDOR protection.
- Project assignment rules.
- Findings management.
- Backend testing.

## AI-assisted workflow

AI was used to support:

- Technical planning.
- Phase-based implementation.
- Code structure decisions.
- Security rule definition.
- Manual testing checklists.
- Documentation.
- Debugging guidance.

All implementation steps were manually reviewed, executed, tested and committed by the developer.

## Development phases

The backend was developed in controlled phases:

1. Environment preparation.
2. PostgreSQL Docker setup.
3. Spring Boot initialization.
4. Domain model and repositories.
5. Cookie-based authentication.
6. RBAC and access control rules.
7. Project CRUD and auditor assignment.
8. Findings CRUD with filters and pagination.
9. Backend authorization tests.
10. Documentation.

## Key decisions

### Java and Spring Boot

Spring Boot was selected to build a structured REST API with strong support for:

- Dependency injection.
- Spring Security.
- Spring Data JPA.
- Validation.
- Testing.

### PostgreSQL

PostgreSQL was selected as the main relational database because the project requires clear relationships between organizations, users, projects and findings.

### Cookie-based JWT authentication

JWT is stored in an HTTPOnly cookie instead of being returned in JSON.

This reduces frontend exposure to token theft through JavaScript.

Rules:

- Token is not returned in JSON.
- Token is not stored in LocalStorage.
- Token is not stored in SessionStorage.
- Cookie is HTTPOnly.
- Cookie uses SameSite=Lax in local development.
- Cookie should use Secure=true in production.

### RBAC

AuditFlow uses two roles:

- ADMIN
- AUDITOR

ADMIN can manage projects and assign auditors.

AUDITOR can only access assigned projects and findings in those projects.

### Multi-tenant isolation

Each user belongs to an organization.

Resources must be filtered by organization to prevent data leaks between tenants.

### IDOR protection

Raw IDs from requests are never trusted directly.

The backend validates:

- Resource ownership.
- Organization membership.
- Auditor assignment.

## Testing approach

Backend testing includes:

- Login success.
- Login failure.
- Protected endpoint without session.
- Admin project creation.
- Auditor project restrictions.
- Auditor finding permissions.
- Filters.
- Pagination.

Manual testing is also documented in the project docs.

## review

AI-generated suggestions were reviewed and adapted during implementation.

The developer validated the project through:

- Local execution.
- Maven builds.
- Automated tests.
- Manual browser testing.
- Git commits per phase.
