# Team Task Tracker API

Spring Boot REST API for organization-scoped team task tracking with JWT authentication, role-based authorization, PostgreSQL persistence, Redis-backed task-list caching, and Docker Compose deployment.

## Run Locally

```bash
docker compose up --build
```

The API starts on `http://localhost:8080`.

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

The application creates a default organization on startup. Use `organizationId: 1` for initial registration. The first user registered in an organization becomes `ADMIN`; later users in the same organization become `MEMBER`. An `ADMIN` can promote a user to `MANAGER` through the user update endpoint.

## Auth Flow

1. Register: `POST /api/v1/auth/register`
2. Login: `POST /api/v1/auth/login`
3. Authorize Swagger with the returned access token.
4. Refresh: `POST /api/v1/auth/refresh`

Refresh token rotation is implemented by revoking the used refresh token and issuing a new access token plus refresh token.

## RBAC Summary

`ADMIN` can manage users and tasks within the organization.

`MANAGER` can create, update, delete, assign, view, and transition tasks within the organization, but cannot manage users.

`MEMBER` can list, view, and transition only tasks assigned to them.

Spring Security method authorization (`@PreAuthorize`) protects controller entry points. Object-level organization and assignee checks are enforced in the service authorization layer before data is returned or changed.

## Task Status Transitions

Tasks start in `TODO`.

Allowed transitions:

```text
TODO -> IN_PROGRESS -> IN_REVIEW -> DONE
TODO -> BLOCKED
IN_PROGRESS -> BLOCKED
IN_REVIEW -> BLOCKED
BLOCKED -> IN_PROGRESS
```

Invalid transitions return a consistent error response.

## Schema Description

`organizations`: stores organization records.

`users`: stores users with `name`, unique `email`, hashed `password`, `role`, and `organization_id`.

`tasks`: stores tasks with `title`, `description`, `priority`, `status`, `assignee_id`, `due_date`, and `created_by`.

`refresh_tokens`: stores refresh tokens with `token`, `expiry_date`, `revoked`, and `user_id`.

## DB Design Decision

Tasks are linked to an organization through their assignee. This keeps each task assigned to a concrete user while still allowing organization-scoped filtering through `assignee.organization.id`.

Indexes are added on `tasks.status`, `tasks.assignee_id`, and `tasks.due_date` because the list endpoint filters by status and assignee, and due date is a common operational query for overdue work.

## Caching Strategy

Task list responses are cached in Redis under the `taskLists` cache. The cache key includes organization, effective assignee, status, priority, page, and limit.

For `MEMBER` users, the effective assignee is always the authenticated user, which prevents one member from reading another member's cached task list.

Cache invalidation is intentionally simple and safe: task create, update, status update, and delete evict all task-list cache entries. This keeps list results fresh after any mutation.

## Error Response Format

Errors use a consistent response shape:

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "due_date must be a future date"
}
```

## Given More Time

I would add project management endpoints because the role summary mentions project ownership, add integration tests for auth/RBAC/task transitions using Testcontainers, add analytics for overdue counts and completion time, and replace `ddl-auto=update` with Flyway or Liquibase migrations.
