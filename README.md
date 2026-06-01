# Team Task Tracker API

Spring Boot REST API for organization-scoped team task tracking with JWT authentication, refresh-token rotation, role-based access control, PostgreSQL persistence, Redis-backed task-list caching, project management, Swagger/OpenAPI documentation, and Docker Compose deployment.

## Quick Start

```bash
docker compose up --build
```

The API starts on:

```text
http://localhost:8080
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON is available at:

```text
http://localhost:8080/v3/api-docs
```

No manual database or Redis setup is required. Docker Compose starts PostgreSQL, Redis, and the application. On startup, the application creates one default organization named `Default Organization`.

## End User Onboarding Flow

Follow this sequence when evaluating the API.

### 1. Register The First User

Request:

```http
POST /api/v1/auth/register
Content-Type: application/json
```

```json
{
  "name": "Admin User",
  "email": "admin@example.com",
  "password": "Password123"
}
```

Behavior:

The first user in the organization becomes `ADMIN` automatically. If `organizationId` is omitted, the user joins the default organization. You may also pass `"organizationId": 1` explicitly.

### 2. Login As Admin

Request:

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "email": "admin@example.com",
  "password": "Password123"
}
```

Response:

```json
{
  "accessToken": "...",
  "refreshToken": "..."
}
```

Use the access token as:

```text
Authorization: Bearer <accessToken>
```

### 3. Register Team Members

Register additional users with the same register endpoint. Every later user in the organization starts as `MEMBER`.

Example:

```json
{
  "name": "Manager User",
  "email": "manager@example.com",
  "password": "Password123"
}
```

Register another user as a normal member:

```json
{
  "name": "Member User",
  "email": "member@example.com",
  "password": "Password123"
}
```

### 4. Get User IDs

As `ADMIN`, list users:

```http
GET /api/v1/users?page=0&limit=10
Authorization: Bearer <adminAccessToken>
```

Only admins can manage users. A user can always view their own profile:

```http
GET /api/v1/users/me
Authorization: Bearer <accessToken>
```

### 5. Promote A User To Manager

As `ADMIN`, update the manager user's role:

```http
PUT /api/v1/users/{managerUserId}
Authorization: Bearer <adminAccessToken>
Content-Type: application/json
```

```json
{
  "name": "Manager User",
  "role": "MANAGER"
}
```

After this, the manager can manage projects and tasks, but cannot manage users.

### 6. Create A Project

As `ADMIN` or `MANAGER`:

```http
POST /api/v1/projects
Authorization: Bearer <managerAccessToken>
Content-Type: application/json
```

```json
{
  "name": "Sprint Board",
  "description": "Tasks for the current sprint"
}
```

The response contains the `projectId`. Projects are scoped to the authenticated user's organization.

### 7. Create A Task

As `ADMIN` or `MANAGER`:

```http
POST /api/v1/tasks
Authorization: Bearer <managerAccessToken>
Content-Type: application/json
```

```json
{
  "title": "Build task API",
  "description": "Implement and verify task endpoints",
  "priority": "HIGH",
  "assigneeId": 3,
  "projectId": 1,
  "dueDate": "2026-06-15"
}
```

Behavior:

Tasks always start in `TODO`. `projectId` is optional because the assignment's required task fields do not require project membership. If a project is supplied, it must belong to the same organization as the authenticated user. The assignee must also belong to the same organization.

### 8. Member Views Assigned Tasks

As `MEMBER`:

```http
GET /api/v1/tasks?page=0&limit=10
Authorization: Bearer <memberAccessToken>
```

Behavior:

Members only see tasks assigned to themselves. Even if a member sends another user's `assigneeId`, the server replaces it with the authenticated member's user id.

Filters:

```http
GET /api/v1/tasks?status=TODO&priority=HIGH&assigneeId=3&page=0&limit=10
```

Supported filters are `status`, `priority`, and `assigneeId`.

### 9. Update Task Status

As the assignee, `MANAGER`, or `ADMIN`:

```http
PATCH /api/v1/tasks/{taskId}/status
Authorization: Bearer <accessToken>
Content-Type: application/json
```

```json
{
  "status": "IN_PROGRESS"
}
```

Allowed transitions:

```text
TODO -> IN_PROGRESS
IN_PROGRESS -> IN_REVIEW
IN_REVIEW -> DONE
TODO -> BLOCKED
IN_PROGRESS -> BLOCKED
IN_REVIEW -> BLOCKED
```

`DONE` and `BLOCKED` are terminal states in this implementation. Invalid transitions return `INVALID_STATUS_TRANSITION`.

### 10. Refresh Tokens

Request:

```http
POST /api/v1/auth/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "<refreshToken>"
}
```

Behavior:

Refresh-token rotation is enforced. The used refresh token is revoked, and the response returns a new access token plus a new refresh token. Reusing the old refresh token returns `401 UNAUTHORIZED`.

## Role Permissions

`ADMIN`

Full access inside the organization. Admins can manage users, projects, and tasks.

`MANAGER`

Can manage projects and tasks inside the organization, assign members to tasks, and transition task status. Managers cannot manage users.

`MEMBER`

Can view their own profile, list only assigned tasks, view only assigned tasks, and update status only for assigned tasks.

RBAC is enforced with Spring Security method authorization through `@PreAuthorize` on controller entry points. Organization and assignee checks are enforced before returning or mutating object-level data.

## Endpoint Summary

Auth:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
```

Users:

```text
GET    /api/v1/users
GET    /api/v1/users/{userId}
PUT    /api/v1/users/{userId}
DELETE /api/v1/users/{userId}
GET    /api/v1/users/me
PUT    /api/v1/users/me
```

Projects:

```text
POST   /api/v1/projects
GET    /api/v1/projects
GET    /api/v1/projects/{projectId}
PUT    /api/v1/projects/{projectId}
DELETE /api/v1/projects/{projectId}
```

Tasks:

```text
POST   /api/v1/tasks
GET    /api/v1/tasks
GET    /api/v1/tasks/{taskId}
PUT    /api/v1/tasks/{taskId}
DELETE /api/v1/tasks/{taskId}
PATCH  /api/v1/tasks/{taskId}/status
```

## Schema Description

`organizations`

Stores organization records.

`users`

Stores users with `name`, unique `email`, hashed `password`, `role`, and `organization_id`.

`projects`

Stores organization-scoped projects with `name`, `description`, `organization_id`, and `created_by`.

`tasks`

Stores tasks with `title`, `description`, `priority`, `status`, `assignee_id`, optional `project_id`, `due_date`, and `created_by`.

`refresh_tokens`

Stores refresh tokens with `token`, `expiry_date`, `revoked`, and `user_id`.

## DB Design Decision

Tasks are organization-scoped through the assignee and optionally linked to a project. This keeps every task owned by a concrete user while still supporting project grouping for ADMIN and MANAGER workflows.

Indexes are added on `tasks.status`, `tasks.assignee_id`, and `tasks.due_date` because the list endpoint filters by status and assignee, and due date is a common operational query for upcoming or overdue work. Projects also have an index on `organization_id` because project lists are always organization-scoped.

## Caching Strategy

Task list responses are cached in Redis under the `taskLists` cache.

The cache key includes:

```text
organization, effective assignee, status, priority, page, limit
```

For `MEMBER` users, the effective assignee is always the authenticated user. This prevents one member from reading another member's cached task list.

Invalidation is intentionally simple and safe. Task create, update, status update, and delete evict all task-list cache entries. Project update and delete also evict task-list cache entries because task responses may include project details.

## Error Response Format

All handled errors use this shape:

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "due_date must be a future date"
}
```

Examples:

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "You do not have permission to access this resource"
}
```

```json
{
  "status": 400,
  "code": "INVALID_STATUS_TRANSITION",
  "message": "Invalid status transition from TODO to DONE"
}
```

## Tests

Run:

```bash
./mvnw test
```

On Windows:

```bash
mvnw.cmd test
```

The integration tests cover critical flows:

```text
member task isolation and task creation denial
status transition validation
refresh-token rotation
project management RBAC
task/project linking
```

Tests use an in-memory H2 database with the `test` profile, so they do not require local PostgreSQL or Redis.

## What I Would Improve With More Time

I would add Flyway or Liquibase migrations instead of `ddl-auto=update`, add Testcontainers-based PostgreSQL and Redis integration tests, add analytics for overdue task counts and average completion time, and add a small task board frontend.
