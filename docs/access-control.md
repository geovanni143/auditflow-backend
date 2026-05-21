# AuditFlow Access Control Rules

## Roles

AuditFlow uses two roles:

- ADMIN
- AUDITOR

## General rules

### ADMIN

An ADMIN can:

- Manage users inside their organization.
- Create and manage projects inside their organization.
- Assign auditors to projects.
- Access findings from projects inside their organization.
- Access organization-level dashboard data.

### AUDITOR

An AUDITOR can:

- Access only projects assigned to them.
- Create findings only in assigned projects.
- Edit findings only in assigned projects.
- View findings only from assigned projects.

## Organization isolation

Users can only access resources inside their own organization.

Cross-organization access must not leak resource existence.

For this reason:

- If a resource does not exist, return 404.
- If a resource exists but belongs to another organization, return 404.
- If a resource exists in the same organization but the user role is not allowed, return 403.

## IDOR protection

The backend must never trust raw IDs from the client without checking ownership or assignment.

Examples:

- `/api/users/{id}` must validate that the target user belongs to the same organization.
- `/api/projects/{id}` must validate that the project belongs to the same organization.
- `/api/findings/{id}` must validate that the finding belongs to a project in the same organization.
- AUDITOR users must also be assigned to the project.

## HTTP status rules

### 401 Unauthorized

Use when:

- No valid authentication cookie exists.
- JWT is missing, expired or invalid.

### 403 Forbidden

Use when:

- The user is authenticated but does not have the required role.
- The user is authenticated but is not assigned to the project.

### 404 Not Found

Use when:

- The resource does not exist.
- The resource exists but belongs to another organization.