# AI Workflow — AuditFlow Backend

## Purpose

This document explains how AI assistance was used during the development of the AuditFlow backend.

AuditFlow is a fullstack MVP for managing security audit projects, assigned auditors and vulnerability findings.

The backend was built with Java and Spring Boot, focusing on:

- Authentication and authorization.
- HTTPOnly cookie-based sessions.
- Role-based access control.
- Organization-based multi-tenant isolation.
- Project management.
- Auditor assignment rules.
- Findings management.
- Filtering and pagination.
- Backend-side security validation.

AI was used as a development assistant, not as a replacement for implementation ownership. All generated suggestions were reviewed, adapted, tested and committed manually.

---

## Tools Used

AI assistance was used through ChatGPT during the development process.

The AI was mainly used for:

- Breaking the project into implementation phases.
- Reviewing technical requirements.
- Suggesting backend structure.
- Suggesting security test scenarios.
- Helping debug deployment and configuration issues.
- Improving documentation quality.
- Reviewing manual testing flows.

The final code, configuration, commits and deployment steps were executed and validated by the developer.

---

## What Was Assisted by AI

AI was used as support in the following areas:

### 1. Planning and Requirement Breakdown

AI helped convert the technical challenge into smaller implementation phases.

Examples:

- Authentication and registration.
- RBAC rules.
- Project CRUD.
- Auditor assignment.
- Findings CRUD.
- Findings filters and pagination.
- Multi-tenant checks.
- Deployment preparation.
- Documentation.

The final scope and implementation order were manually reviewed and adjusted during development.

---

### 2. Security Design Support

AI was used to discuss and validate security requirements such as:

- Avoiding token storage in LocalStorage.
- Avoiding token storage in SessionStorage.
- Using HTTPOnly cookies for session handling.
- Enforcing authorization in the backend.
- Preventing auditors from accessing unassigned projects.
- Preventing cross-tenant access between organizations.
- Returning appropriate HTTP status codes.

The final backend rules were implemented and tested manually.

---

### 3. Documentation Support

AI was used to improve documentation structure for:

- README.md.
- Database model explanation.
- Manual testing checklist.
- Deployment notes.
- AI workflow explanation.

The documentation was reviewed and adapted to match the real implementation.

---

### 4. Debugging Support

AI was used to reason about some issues found during development and deployment, including:

- Cookie behavior between frontend and backend domains.
- Render deployment configuration.
- Database seeding behavior.
- Production environment variables.
- Multi-tenant testing scenarios.
- Session validation flow.

The fixes were applied manually and verified by running the application.

---

## What Was Implemented and Reviewed by the Developer

The developer was responsible for:

- Creating and configuring the Spring Boot project.
- Defining the domain entities.
- Creating repositories.
- Implementing service logic.
- Implementing controllers.
- Configuring Spring Security.
- Configuring JWT creation and validation.
- Configuring HTTPOnly cookies.
- Implementing authorization checks.
- Creating and testing project assignment rules.
- Implementing findings filters and pagination.
- Running Maven builds.
- Testing endpoints with curl.
- Testing the deployed application manually.
- Managing Git commits.
- Deploying the backend to Render.
- Connecting the backend to Neon PostgreSQL.

AI suggestions were not accepted blindly. They were checked against the project requirements and adjusted when necessary.

---

## Development Phases

The backend was developed in controlled phases.

### Phase 1 — Environment Setup

- Java runtime configured.
- Maven project prepared.
- PostgreSQL local environment prepared.
- Spring Boot application started.

### Phase 2 — Domain Model

Main entities were created:

- Organization.
- User.
- Project.
- ProjectAuditor.
- Finding.

The model was designed to support organization-based isolation and auditor assignments.

### Phase 3 — Authentication

Implemented:

- Register.
- Login.
- Logout.
- Current user endpoint.
- Password hashing.
- JWT creation.
- HTTPOnly cookie session.

### Phase 4 — Authorization and RBAC

Implemented two roles:

- ADMIN.
- AUDITOR.

Rules:

- Admin can manage users and projects.
- Auditor can only work with assigned projects.
- Authorization is enforced in the backend.

### Phase 5 — Projects

Implemented project operations:

- List projects.
- Create project.
- Update project.
- Delete project.
- Assign auditor to project.

### Phase 6 — Findings

Implemented finding operations:

- List findings.
- Create finding.
- Update finding.
- Delete finding.
- Filter by severity.
- Filter by status.
- Pagination support.

### Phase 7 — Multi-tenant Validation

Implemented and tested organization isolation rules:

- Users belong to one organization.
- Projects belong to one organization.
- Findings are scoped through projects.
- Cross-organization access is blocked.

### Phase 8 — Deployment

Backend deployment was configured using:

- Render.
- Docker.
- Neon PostgreSQL.
- Environment variables.

### Phase 9 — Documentation

Documentation was added for:

- Local setup.
- Environment variables.
- API overview.
- Database model.
- Demo credentials.
- Deployment notes.
- AI workflow.

---

## Key Technical Decisions

### Java and Spring Boot

Spring Boot was used because the challenge required Java and because it provides strong support for:

- REST APIs.
- Dependency injection.
- Spring Security.
- Spring Data JPA.
- Validation.
- Production deployment.

### PostgreSQL

PostgreSQL was used because the domain has clear relational requirements:

- Organizations own users.
- Organizations own projects.
- Projects have assigned auditors.
- Projects contain findings.

### HTTPOnly Cookie Sessions

The backend stores the JWT in an HTTPOnly cookie.

This decision was made to avoid exposing the token to frontend JavaScript.

Session rules:

- No token in LocalStorage.
- No token in SessionStorage.
- No token returned as frontend-managed state.
- Cookie is HTTPOnly.
- Cookie is Secure in production.
- Cookie uses SameSite configuration based on the environment.

### Backend Authorization

The frontend hides or shows UI elements depending on the role, but security is enforced in the backend.

This means a user cannot bypass authorization simply by calling the API directly.

Examples:

- Auditors cannot manage users.
- Auditors cannot create projects.
- Auditors cannot update projects.
- Auditors cannot access unassigned projects.
- Users cannot access another organization’s data.

### Multi-tenant Isolation

Each user belongs to an organization.

Projects are owned by an organization.

Findings belong to projects, and therefore inherit the organization scope from the project.

This structure was used to reduce the risk of IDOR and cross-tenant data exposure.

---

## AI Mistakes and Corrections

During the project, some AI suggestions required correction.

### Mistake 1 — Production Cookie Behavior

Some early guidance assumed that cross-domain cookies between the Vercel frontend and Render backend would work without additional adjustments.

In practice, the deployed frontend and backend use different domains, which made session handling more sensitive.

Correction:

- API requests were routed through the frontend domain using a Next.js rewrite/proxy.
- The frontend API client was adjusted to call relative `/api/...` paths.
- Requests still use `credentials: "include"`.
- The backend remains responsible for setting and validating the HTTPOnly cookie.

This improved session stability in production.

---

### Mistake 2 — Automatic Database Seeder

An initial seed process was useful during development, but it attempted to insert demo users again when the backend restarted in production.

This caused a duplicate email conflict for:

```txt
admin@auditflow.local
