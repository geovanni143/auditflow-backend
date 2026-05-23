# AI Workflow — AuditFlow Backend

## Purpose

This document explains how AI tools were used during the development of the AuditFlow backend.

AuditFlow is a fullstack MVP for managing security audit projects, assigned auditors and vulnerability findings. The backend was built with Java and Spring Boot, with a focus on authentication, authorization, project management, findings management and multi-tenant data isolation.

AI was used as a support tool during development, mainly for planning, reviewing ideas, debugging issues and improving documentation. The final implementation decisions, code changes, testing and commits were handled manually by the developer.

---

## AI Tools Used

The main AI tools used during the project were:

- ChatGPT
- Claude

These tools were used as development assistants to help organize the work, review the technical requirements, reason about backend security rules and troubleshoot implementation issues.

AI suggestions were not accepted automatically. Each relevant suggestion was reviewed, adjusted to the real project, tested locally or in production, and committed manually.

---

## How AI Was Used

AI was used in a practical way during the development process.

The main uses were:

- Breaking the technical challenge into smaller tasks.
- Reviewing the required modules and security rules.
- Discussing backend structure and responsibilities.
- Creating manual testing checklists.
- Reviewing possible RBAC and multi-tenant scenarios.
- Helping debug deployment and production configuration issues.
- Improving the wording and structure of project documentation.

The actual implementation was still reviewed and controlled manually. I did not treat AI output as automatically correct.

---

## What I Implemented and Reviewed Manually

The following parts were implemented, reviewed and tested manually:

- Spring Boot project setup.
- Domain model creation.
- Entity relationships.
- Repository definitions.
- Service-layer logic.
- Controllers and API endpoints.
- Spring Security configuration.
- JWT generation and validation.
- HTTPOnly cookie handling.
- Role-based access control.
- Project CRUD rules.
- Auditor assignment rules.
- Findings CRUD.
- Findings filters and pagination.
- Multi-tenant checks.
- Deployment configuration.
- Manual API testing with curl.
- Production testing with Vercel, Render and Neon.
- Git commits and repository organization.

AI helped with guidance and review, but the final code was tested directly by running the project and validating the behavior.

---

## Development Phases

The backend was developed in phases to keep the implementation controlled and easier to test.

### Phase 1 — Environment and Project Setup

The backend project was prepared with Java, Spring Boot and Maven.

PostgreSQL was used as the relational database because the domain requires clear relationships between organizations, users, projects, assigned auditors and findings.

---

### Phase 2 — Domain Model

The main entities were created:

- Organization
- User
- Project
- ProjectAuditor
- Finding

The model was designed around organizations because the system supports multiple tenants. Each user belongs to an organization, and each project belongs to an organization. Findings belong to projects, so they are also isolated through the project organization.

---

### Phase 3 — Authentication

Authentication was implemented with:

- Registration.
- Login.
- Logout.
- Current user endpoint.
- Password hashing.
- JWT creation.
- HTTPOnly cookie session.

The session token is not stored in LocalStorage or SessionStorage. The backend stores it in an HTTPOnly cookie.

---

### Phase 4 — Authorization and RBAC

Two roles were implemented:

- ADMIN
- AUDITOR

The intended behavior is:

- Admins manage their organization, projects and users.
- Auditors only interact with projects assigned to them.

The important part is that authorization is enforced in the backend, not only by hiding buttons in the frontend.

---

### Phase 5 — Project Management

Project management was implemented for admins.

Admins can:

- Create projects.
- List projects in their organization.
- Update projects.
- Delete projects.
- Assign auditors to projects.

Auditors can only see projects assigned to them.

---

### Phase 6 — Findings Management

Findings were implemented as vulnerabilities associated with a project.

A finding includes:

- Title.
- Description.
- Recommendation.
- Evidence.
- Severity.
- Status.
- Related project.
- Reporting user.

Findings support filtering and pagination.

---

### Phase 7 — Multi-tenant Isolation

The backend was reviewed and tested to make sure users from one organization cannot access another organization’s data.

This was important because the challenge mentions IDOR and multi-tenant data isolation.

The application was tested with two organizations:

- N3X Security
- Acme Security Labs

The goal was to confirm that users, projects and findings are isolated by organization.

---

### Phase 8 — Deployment

The backend was deployed using:

- Render for the backend.
- Neon PostgreSQL for the database.
- Docker for the Render deployment.

Production environment variables were configured for database connection, JWT configuration, cookies and CORS.

---

### Phase 9 — Documentation

Documentation was added and improved for:

- Local setup.
- Environment variables.
- API overview.
- Demo credentials.
- Deployment notes.
- Database model.
- AI workflow.

AI helped improve the structure and clarity of the documentation, but the content was adjusted to match the real implementation.

---

## Key Technical Decisions

### Java and Spring Boot

Spring Boot was used because the challenge required Java and because it provides strong support for REST APIs, dependency injection, Spring Security, validation and database access through Spring Data JPA.

---

### PostgreSQL

PostgreSQL was selected because the application is relational by nature.

The main relationships are:

- Organizations have users.
- Organizations have projects.
- Projects have assigned auditors.
- Projects have findings.
- Users report findings.

---

### HTTPOnly Cookie Sessions

The backend stores the JWT in an HTTPOnly cookie.

This was chosen to avoid exposing the token to frontend JavaScript.

Session rules:

- No token in LocalStorage.
- No token in SessionStorage.
- No token returned for the frontend to manually store.
- Cookie is HTTPOnly.
- Cookie is configured as Secure in production.

---

### Backend Authorization

The frontend can improve the user experience by showing or hiding actions based on the role, but the backend is responsible for real authorization.

For example:

- An auditor cannot create projects by calling the API directly.
- An auditor cannot update projects by calling the API directly.
- An auditor cannot manage users.
- An auditor cannot access projects that are not assigned to them.
- A user cannot access resources from another organization.

---

### Multi-tenant Isolation

Each user belongs to one organization.

Each project belongs to one organization.

Each finding belongs to a project, so the finding is scoped through the project organization.

This design helps prevent cross-tenant data leaks and IDOR issues.

---

## AI-Assisted Areas

AI was most useful in these areas:

### Planning

AI helped turn the requirements into smaller phases and checklists.

This helped keep the implementation organized instead of trying to build everything at once.

---

### Security Review

AI was used to discuss security scenarios such as:

- What an auditor should not be able to do.
- How to avoid storing tokens in browser storage.
- How to test IDOR scenarios.
- How to verify organization isolation.
- What HTTP status codes should be returned in common cases.

The final behavior was tested manually through the API and browser.

---

### Debugging

AI was used to reason about issues that appeared during development and deployment.

Some examples:

- Cookie behavior between Vercel and Render.
- Backend session validation.
- Render deployment failures.
- Neon database connection and seed data.
- Protected routes briefly showing content before session validation.

The fixes were applied manually and tested after deployment.

---

### Documentation

AI helped make the documentation clearer and more complete.

The README and this file were reviewed so they describe the real project instead of describing a generic application.

---

## AI Mistakes and Corrections

AI was useful, but it was not always correct. Some suggestions had to be adjusted during development.

### 1. Cookie Behavior in Production

At first, the session flow worked locally, but production had an issue because the frontend and backend were deployed on different domains.

The frontend was on Vercel and the backend was on Render. Because of that, cookie behavior was more sensitive than in local development.

What was corrected:

- The frontend API client was adjusted to use relative `/api/...` paths.
- Next.js rewrites were used to proxy API requests through the frontend domain.
- Requests still use `credentials: "include"`.
- The backend remains responsible for creating and validating the HTTPOnly cookie.

This made the session more stable in production.

---

### 2. Automatic Database Seeder

A database seeder was useful during development, but in production it caused a problem after the database already had demo users.

The backend attempted to insert the same demo email again:

```txt
admin@auditflow.local
```

That caused a duplicate email error in PostgreSQL and prevented the backend from starting correctly.

What was corrected:

- The seeder was changed so it does not run automatically by default.
- Production demo data was handled manually.
- The backend no longer crashes when Render restarts.

---

### 3. Protected Page Flash

A frontend issue appeared when opening protected routes directly.

For a moment, a protected page could render before the session validation finished and redirected the user to login.

What was corrected:

- Protected pages were wrapped with a route guard.
- A safe loading state is shown while the session is validated.
- Unauthenticated users are redirected to login.
- Backend authorization remains the final source of truth.

This improved the user experience and prevented protected information from appearing during the initial render.

---

## Testing Approach

Testing was done through a mix of local builds, API tests and browser testing.

---

## Build Verification

The backend was compiled with Maven:

```bash
mvn clean compile
```

This was used to confirm that the backend compiled correctly before committing changes.

---

## Authentication Testing

Tested scenarios included:

- Registering an organization.
- Logging in as Admin.
- Logging in as Auditor.
- Logging out.
- Calling `/api/auth/me` with a valid session.
- Calling protected endpoints without a valid session.

---

## Authorization Testing

Tested scenarios included:

- Admin can list users.
- Admin can create auditors.
- Admin can create projects.
- Admin can assign auditors to projects.
- Auditor cannot list users.
- Auditor cannot create users.
- Auditor cannot create projects.
- Auditor cannot update projects.
- Auditor only sees assigned projects.

---

## Findings Testing

Tested scenarios included:

- Auditor can create findings in assigned projects.
- Auditor cannot create findings in unassigned projects.
- Findings can be filtered by severity.
- Findings can be filtered by status.
- Findings support pagination.

---

## Multi-tenant Testing

A second organization was used to validate tenant isolation.

Organizations used:

- N3X Security
- Acme Security Labs

Tested scenarios included:

- N3X users cannot see Acme data.
- Acme users cannot see N3X data.
- Cross-tenant project access is blocked.
- Cross-tenant findings are not exposed.

---

## Deployment Testing

Production was validated with:

- Render backend deployment.
- Vercel frontend deployment.
- Neon PostgreSQL database.
- Backend health check.
- Demo credentials.
- Browser testing.
- Direct API testing with `curl`.

---

## Manual Review Process

Before committing important changes, I reviewed:

- Whether the code matched the technical challenge.
- Whether the backend enforced the required security rules.
- Whether protected data was isolated by organization.
- Whether auditors were limited to assigned projects.
- Whether the application compiled.
- Whether endpoints returned the expected HTTP status codes.
- Whether production environment variables were configured correctly.
- Whether the deployed application could be used with the demo credentials.

---

## What I Can Explain

I can explain the main technical decisions and implementation details, including:

- Why HTTPOnly cookies were used.
- Why LocalStorage and SessionStorage were avoided.
- How JWT validation works at a high level.
- How Admin and Auditor roles are enforced.
- How auditor assignment works.
- How projects are scoped to organizations.
- How findings are scoped through projects.
- How the backend reduces IDOR risk.
- How the deployment uses Render, Vercel and Neon.
- Why the database seeder was changed for production.
- Why the frontend uses a proxy for API requests in production.

---

## Final Notes

AI was helpful during the project, especially for planning, reviewing, debugging and documentation.

However, the final result was not accepted blindly from AI output. The implementation was manually reviewed, tested and adjusted to match the technical challenge.

The main goal was to keep the application understandable, functional and secure enough for the MVP scope.
