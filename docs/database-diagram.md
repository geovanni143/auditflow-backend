# AuditFlow Database Diagram

## Overview

AuditFlow uses PostgreSQL as the main relational database.

The data model is designed around multi-tenant isolation. Every user belongs to one organization, and every project belongs to one organization. Findings belong to projects, so their organization is derived through the related project.

The system supports two roles:

- `ADMIN`: manages users, projects and auditor assignments inside their own organization.
- `AUDITOR`: can only interact with projects assigned to them.

## Main Entities

- `organizations`
- `app_users`
- `projects`
- `project_auditors`
- `findings`

## Entity Relationship Diagram

```mermaid
erDiagram
    ORGANIZATIONS {
        bigint id PK
        varchar name UK
        varchar description
        boolean active
        timestamp created_at
        timestamp updated_at
    }

    APP_USERS {
        bigint id PK
        bigint organization_id FK
        varchar full_name
        varchar email UK
        varchar password_hash
        varchar role
        boolean enabled
        timestamp created_at
        timestamp updated_at
    }

    PROJECTS {
        bigint id PK
        bigint organization_id FK
        bigint created_by_id FK
        varchar name
        varchar description
        varchar target
        varchar status
        timestamp created_at
        timestamp updated_at
    }

    PROJECT_AUDITORS {
        bigint id PK
        bigint project_id FK
        bigint auditor_id FK
        timestamp assigned_at
    }

    FINDINGS {
        bigint id PK
        bigint project_id FK
        bigint reported_by_id FK
        varchar title
        varchar description
        varchar recommendation
        varchar evidence
        varchar severity
        varchar status
        timestamp created_at
        timestamp updated_at
    }

    ORGANIZATIONS ||--o{ APP_USERS : contains
    ORGANIZATIONS ||--o{ PROJECTS : owns

    APP_USERS ||--o{ PROJECTS : creates

    PROJECTS ||--o{ PROJECT_AUDITORS : has_assignments
    APP_USERS ||--o{ PROJECT_AUDITORS : assigned_as_auditor

    PROJECTS ||--o{ FINDINGS : contains
    APP_USERS ||--o{ FINDINGS : reports