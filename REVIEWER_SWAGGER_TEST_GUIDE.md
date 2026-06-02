# Reviewer Swagger Test Guide

This guide explains how to validate the Team Task Tracker API end to end using Swagger UI. It is written for a reviewer who is starting from a fresh clone and wants to verify authentication, roles, users, projects, tasks, refresh-token rotation, validation, error handling, caching behavior, and Docker readiness.

## 1. Application Summary

This API manages tasks inside an organization.

Users belong to an organization and have one of three roles:

```text
ADMIN -> highest privilege
MANAGER -> project/task manager
MEMBER -> assigned-task user
```

The first registered user in an organization becomes `ADMIN`. Later users in the same organization become `MEMBER` by default. An `ADMIN` can promote a user to `MANAGER`.

## 2. Role Hierarchy And Permissions

### ADMIN

An `ADMIN` has full access inside their organization.

Allowed:

```text
Manage users
Manage projects
Manage tasks
Assign tasks
View all organization tasks
Update task status
Refresh own token
View/update own profile
```

Typical admin checks:

```text
GET    /api/v1/users
GET    /api/v1/users/{userId}
PUT    /api/v1/users/{userId}
DELETE /api/v1/users/{userId}
POST   /api/v1/projects
POST   /api/v1/tasks
GET    /api/v1/tasks
```

### MANAGER

A `MANAGER` can manage projects and tasks inside the organization but cannot manage users.

Allowed:

```text
Create/list/view/update/delete projects
Create/list/view/update/delete tasks
Assign tasks to organization users
Update task status
View/update own profile
Refresh own token
```

Not allowed:

```text
List users
View arbitrary user details
Update user roles
Delete users
```

Expected response for user-management attempts:

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "You do not have permission to access this resource"
}
```

### MEMBER

A `MEMBER` can only work with tasks assigned to them.

Allowed:

```text
View own profile
Update own profile name
List only assigned tasks
View assigned task details
Advance assigned task status
Refresh own token
```

Not allowed:

```text
Manage users
Manage projects
Create tasks
Update task details
Delete tasks
View another member's task
Update another member's task status
```

Expected response for unauthorized object access:

```json
{
  "status": 403,
  "code": "FORBIDDEN",
  "message": "You are not authorized to update/view this task"
}
```

## 3. Start The Application

From the repository root:

```bash
docker compose up --build
```

Expected containers:

```text
tasktracker-app
tasktracker-postgres
tasktracker-redis
```

Open Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

Open OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

If you want a completely clean review database:

```bash
docker compose down -v
docker compose up --build
```

## 4. How To Use Swagger Authorization

For login requests, no token is needed.

After login, copy the returned `accessToken`.

In Swagger UI:

1. Click `Authorize`.
2. Enter:

```text
Bearer <accessToken>
```

3. Click `Authorize`.
4. Click `Close`.

Whenever the guide says "switch to admin token", "switch to manager token", or "switch to member token", repeat the same Swagger authorization step with that user's access token.

### If Swagger Shows Invalid Or Expired Access Token

If you see this response:

```json
{
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "Invalid or expired access token"
}
```

It usually means Swagger is still sending an old bearer token in the `Authorization` header.

Immediate recovery steps:

1. Click `Authorize` in Swagger.
2. Click `Logout`.
3. Close the authorize popup.
4. Use `POST /api/v1/auth/login` again with email and password.
5. Copy the new `accessToken`.
6. Click `Authorize` again and enter:

```text
Bearer <newAccessToken>
```

Then continue testing.

Auth endpoints are designed to be recovery endpoints:

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
```

These endpoints do not require an access token. If the access token expires, use login again, or use `/api/v1/auth/refresh` with a valid refresh token to get a new access token.

## 5. Test Data To Create

Use these users during review:

```text
Admin:
email: admin@example.com
password: Password123

Manager:
email: manager@example.com
password: Password123

Member:
email: member@example.com
password: Password123

Other Member:
email: other-member@example.com
password: Password123
```

Use due dates safely in the future. For example:

```text
2030-12-31
```

## 6. End-To-End Happy Path

### Step 1. Register The First User As Admin

Swagger endpoint:

```text
POST /api/v1/auth/register
```

Request:

```json
{
  "name": "Admin User",
  "email": "admin@example.com",
  "password": "Password123"
}
```

Expected:

```text
201 Created
```

Response:

```json
{
  "message": "User registered successfully"
}
```

Why this matters:

The first user in the default organization should become `ADMIN` automatically.

### Step 2. Login As Admin

Swagger endpoint:

```text
POST /api/v1/auth/login
```

Request:

```json
{
  "email": "admin@example.com",
  "password": "Password123"
}
```

Expected:

```text
200 OK
```

Save:

```text
adminAccessToken
adminRefreshToken
```

Authorize Swagger with:

```text
Bearer <adminAccessToken>
```

### Step 3. Verify Admin Profile

Swagger endpoint:

```text
GET /api/v1/users/me
```

Expected:

```text
200 OK
```

Confirm:

```text
email = admin@example.com
role = ADMIN
```

Save:

```text
adminUserId
```

### Step 4. Register Manager, Member, And Other Member

Use the same register endpoint three times.

Manager request:

```json
{
  "name": "Manager User",
  "email": "manager@example.com",
  "password": "Password123"
}
```

Member request:

```json
{
  "name": "Member User",
  "email": "member@example.com",
  "password": "Password123"
}
```

Other member request:

```json
{
  "name": "Other Member User",
  "email": "other-member@example.com",
  "password": "Password123"
}
```

Expected for each:

```text
201 Created
```

### Step 5. Login Each User And Save Tokens

Login as manager:

```json
{
  "email": "manager@example.com",
  "password": "Password123"
}
```

Login as member:

```json
{
  "email": "member@example.com",
  "password": "Password123"
}
```

Login as other member:

```json
{
  "email": "other-member@example.com",
  "password": "Password123"
}
```

Save:

```text
managerAccessToken
managerRefreshToken
memberAccessToken
memberRefreshToken
otherMemberAccessToken
```

### Step 6. As Admin, List Users

Switch Swagger authorization back to:

```text
Bearer <adminAccessToken>
```

Swagger endpoint:

```text
GET /api/v1/users?page=0&limit=10
```

Expected:

```text
200 OK
```

Confirm all users are present. Save:

```text
managerUserId
memberUserId
otherMemberUserId
```

Expected roles before promotion:

```text
Admin User -> ADMIN
Manager User -> MEMBER
Member User -> MEMBER
Other Member User -> MEMBER
```

### Step 7. Promote Manager User

Swagger endpoint:

```text
PUT /api/v1/users/{managerUserId}
```

Request:

```json
{
  "name": "Manager User",
  "role": "MANAGER"
}
```

Expected:

```text
200 OK
```

Confirm:

```text
role = MANAGER
```

### Step 8. As Manager, Create A Project

Switch Swagger authorization to:

```text
Bearer <managerAccessToken>
```

Swagger endpoint:

```text
POST /api/v1/projects
```

Request:

```json
{
  "name": "Sprint Board",
  "description": "Tasks for current sprint"
}
```

Expected:

```text
201 Created
```

Save:

```text
projectId
```

### Step 9. As Manager, List Projects

Swagger endpoint:

```text
GET /api/v1/projects?page=0&limit=10
```

Expected:

```text
200 OK
```

Confirm:

```text
Sprint Board is listed
```

### Step 10. As Manager, Get Project By ID

Swagger endpoint:

```text
GET /api/v1/projects/{projectId}
```

Expected:

```text
200 OK
```

Confirm:

```text
id = projectId
name = Sprint Board
```

### Step 11. As Manager, Update Project

Swagger endpoint:

```text
PUT /api/v1/projects/{projectId}
```

Request:

```json
{
  "name": "Sprint Board Updated",
  "description": "Updated sprint project"
}
```

Expected:

```text
200 OK
```

Confirm:

```text
name = Sprint Board Updated
```

### Step 12. As Manager, Create A Task Assigned To Member

Swagger endpoint:

```text
POST /api/v1/tasks
```

Request:

```json
{
  "title": "Build task API",
  "description": "Implement task endpoints",
  "priority": "HIGH",
  "assigneeId": 3,
  "projectId": 1,
  "dueDate": "2030-12-31"
}
```

Replace:

```text
assigneeId -> memberUserId
projectId -> projectId
```

Expected:

```text
201 Created
```

Confirm:

```text
status = TODO
priority = HIGH
assigneeId = memberUserId
projectId = projectId
```

Save:

```text
memberTaskId
```

### Step 13. As Manager, Create A Task Assigned To Other Member

Swagger endpoint:

```text
POST /api/v1/tasks
```

Request:

```json
{
  "title": "Other member task",
  "description": "Task assigned to another member",
  "priority": "MEDIUM",
  "assigneeId": 4,
  "projectId": 1,
  "dueDate": "2030-12-31"
}
```

Replace:

```text
assigneeId -> otherMemberUserId
projectId -> projectId
```

Expected:

```text
201 Created
```

Save:

```text
otherMemberTaskId
```

### Step 14. As Manager, List All Tasks

Swagger endpoint:

```text
GET /api/v1/tasks?page=0&limit=10
```

Expected:

```text
200 OK
```

Confirm:

```text
Both memberTaskId and otherMemberTaskId are visible
```

### Step 15. As Manager, Filter Tasks

Test by status:

```text
GET /api/v1/tasks?status=TODO&page=0&limit=10
```

Test by priority:

```text
GET /api/v1/tasks?priority=HIGH&page=0&limit=10
```

Test by assignee:

```text
GET /api/v1/tasks?assigneeId=<memberUserId>&page=0&limit=10
```

Expected:

```text
200 OK
Filtered results only
```

### Step 16. As Member, List Assigned Tasks

Switch Swagger authorization to:

```text
Bearer <memberAccessToken>
```

Swagger endpoint:

```text
GET /api/v1/tasks?page=0&limit=10
```

Expected:

```text
200 OK
```

Confirm:

```text
Only memberTaskId is visible
otherMemberTaskId is not visible
```

### Step 17. As Member, Get Assigned Task By ID

Swagger endpoint:

```text
GET /api/v1/tasks/{memberTaskId}
```

Expected:

```text
200 OK
```

Confirm task details are returned.

### Step 18. As Member, Advance Assigned Task Status

Allowed transition sequence:

```text
TODO -> IN_PROGRESS -> IN_REVIEW -> DONE
```

First request:

```text
PATCH /api/v1/tasks/{memberTaskId}/status
```

```json
{
  "status": "IN_PROGRESS"
}
```

Expected:

```text
200 OK
status = IN_PROGRESS
```

Second request:

```json
{
  "status": "IN_REVIEW"
}
```

Expected:

```text
200 OK
status = IN_REVIEW
```

Third request:

```json
{
  "status": "DONE"
}
```

Expected:

```text
200 OK
status = DONE
```

### Step 19. Test BLOCKED Transition On A New Active Task

Create another task as manager assigned to member, then switch back to member.

From `TODO`, update status:

```json
{
  "status": "BLOCKED"
}
```

Expected:

```text
200 OK
status = BLOCKED
```

Note:

`BLOCKED` is terminal in this implementation.

### Step 20. As Manager, Update Task Details

Switch Swagger authorization to:

```text
Bearer <managerAccessToken>
```

Swagger endpoint:

```text
PUT /api/v1/tasks/{otherMemberTaskId}
```

Request:

```json
{
  "title": "Other member task updated",
  "description": "Updated details",
  "priority": "LOW",
  "assigneeId": 4,
  "projectId": 1,
  "dueDate": "2030-12-31"
}
```

Replace IDs with actual values.

Expected:

```text
200 OK
```

Confirm:

```text
title updated
priority = LOW
```

### Step 21. As Admin, Delete A Task

Switch Swagger authorization to:

```text
Bearer <adminAccessToken>
```

Swagger endpoint:

```text
DELETE /api/v1/tasks/{taskId}
```

Use a task that is no longer needed for later checks.

Expected:

```text
200 OK
```

Response:

```json
{
  "message": "Task deleted successfully"
}
```

### Step 22. Refresh Token Rotation

Use the member refresh token saved earlier.

Swagger endpoint:

```text
POST /api/v1/auth/refresh
```

Request:

```json
{
  "refreshToken": "<memberRefreshToken>"
}
```

Expected:

```text
200 OK
```

Save:

```text
newMemberAccessToken
newMemberRefreshToken
```

Now call the same endpoint again with the old `memberRefreshToken`.

Expected:

```text
401 Unauthorized
```

Expected response:

```json
{
  "status": 401,
  "code": "UNAUTHORIZED",
  "message": "Refresh token already revoked"
}
```

Authorize Swagger with the new access token and call:

```text
GET /api/v1/users/me
```

Expected:

```text
200 OK
```

## 7. Sad Path And Security Checks

Run these checks after the happy path.

### Check 1. No Token

Remove Swagger authorization.

Call:

```text
GET /api/v1/users/me
```

Expected:

```text
401 Unauthorized
```

### Check 2. Wrong Password

Call:

```text
POST /api/v1/auth/login
```

Request:

```json
{
  "email": "admin@example.com",
  "password": "WrongPassword123"
}
```

Expected:

```text
401 Unauthorized
```

### Check 3. Duplicate Email

Call:

```text
POST /api/v1/auth/register
```

Request:

```json
{
  "name": "Duplicate Admin",
  "email": "admin@example.com",
  "password": "Password123"
}
```

Expected:

```text
409 Conflict
```

### Check 4. Manager Cannot Manage Users

Authorize with:

```text
Bearer <managerAccessToken>
```

Call:

```text
GET /api/v1/users?page=0&limit=10
```

Expected:

```text
403 Forbidden
```

Call:

```text
PUT /api/v1/users/{memberUserId}
```

Request:

```json
{
  "name": "Member User",
  "role": "MANAGER"
}
```

Expected:

```text
403 Forbidden
```

### Check 5. Member Cannot Manage Projects

Authorize with:

```text
Bearer <memberAccessToken>
```

Call:

```text
GET /api/v1/projects?page=0&limit=10
```

Expected:

```text
403 Forbidden
```

Call:

```text
POST /api/v1/projects
```

Request:

```json
{
  "name": "Not Allowed",
  "description": "Member should not create this"
}
```

Expected:

```text
403 Forbidden
```

### Check 6. Member Cannot Create Task

Authorize with:

```text
Bearer <memberAccessToken>
```

Call:

```text
POST /api/v1/tasks
```

Request:

```json
{
  "title": "Not allowed task",
  "description": "Member cannot create task",
  "priority": "LOW",
  "assigneeId": 3,
  "projectId": 1,
  "dueDate": "2030-12-31"
}
```

Expected:

```text
403 Forbidden
```

### Check 7. Member Cannot View Another Member's Task

Authorize with:

```text
Bearer <memberAccessToken>
```

Call:

```text
GET /api/v1/tasks/{otherMemberTaskId}
```

Expected:

```text
403 Forbidden
```

Expected code:

```text
FORBIDDEN
```

### Check 8. Member Cannot Update Another Member's Task Status

Authorize with:

```text
Bearer <memberAccessToken>
```

Call:

```text
PATCH /api/v1/tasks/{otherMemberTaskId}/status
```

Request:

```json
{
  "status": "IN_PROGRESS"
}
```

Expected:

```text
403 Forbidden
```

### Check 9. Invalid Status Transition

Create a new task as manager. It starts as `TODO`.

Authorize as assigned member and call:

```text
PATCH /api/v1/tasks/{newTodoTaskId}/status
```

Request:

```json
{
  "status": "DONE"
}
```

Expected:

```text
400 Bad Request
```

Expected code:

```text
INVALID_STATUS_TRANSITION
```

### Check 10. Missing Required Task Title

Authorize as manager.

Call:

```text
POST /api/v1/tasks
```

Request:

```json
{
  "description": "Missing title",
  "priority": "HIGH",
  "assigneeId": 3,
  "projectId": 1,
  "dueDate": "2030-12-31"
}
```

Expected:

```text
400 Bad Request
```

Expected code:

```text
VALIDATION_ERROR
```

### Check 11. Past Due Date

Authorize as manager.

Call:

```text
POST /api/v1/tasks
```

Request:

```json
{
  "title": "Past due task",
  "description": "This should fail",
  "priority": "HIGH",
  "assigneeId": 3,
  "projectId": 1,
  "dueDate": "2020-01-01"
}
```

Expected:

```text
400 Bad Request
```

Expected message:

```text
due_date must be a future date
```

### Check 12. Invalid Enum

Call:

```text
GET /api/v1/tasks?priority=URGENT&page=0&limit=10
```

Expected:

```text
400 Bad Request
```

Expected code:

```text
VALIDATION_ERROR
```

### Check 13. Invalid Pagination

Call:

```text
GET /api/v1/tasks?page=-1&limit=10
```

Expected:

```text
400 Bad Request
```

Call:

```text
GET /api/v1/tasks?page=0&limit=101
```

Expected:

```text
400 Bad Request
```

### Check 14. Delete Project With Linked Tasks

Authorize as manager or admin.

Call:

```text
DELETE /api/v1/projects/{projectId}
```

Expected if tasks are still linked:

```text
400 Bad Request
```

Expected message:

```text
Project cannot be deleted while tasks are linked to it
```

To verify successful project deletion, first delete all linked tasks, then call the delete project endpoint again.

Expected after linked tasks are deleted:

```text
200 OK
```

## 8. Caching Validation

The API caches task list responses in Redis.

Main cached endpoint:

```text
GET /api/v1/tasks
```

Cache key includes:

```text
organization
effective assignee
status
priority
page
limit
```

For members, the effective assignee is always the logged-in member, even if they pass another `assigneeId`.

Suggested Swagger validation:

1. Authorize as member.
2. Call:

```text
GET /api/v1/tasks?page=0&limit=10
```

3. Call it again with the same parameters.
4. Confirm response is the same.
5. Authorize as manager.
6. Update one of the member's tasks.
7. Authorize as member again.
8. Call the same list endpoint.
9. Confirm the updated task data is visible.

This validates the documented invalidation strategy:

```text
task create/update/status update/delete -> evict task list cache
project update/delete -> evict task list cache
```

Optional Redis CLI check:

```bash
docker compose exec redis redis-cli KEYS '*taskLists*'
```

## 9. Error Response Shape

All handled errors should follow this shape:

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "due_date must be a future date"
}
```

Reviewer should verify this shape for:

```text
401 authentication failures
403 forbidden access
400 validation errors
400 invalid status transitions
404 missing resources
409 duplicate resources
```

## 10. Endpoint Coverage Checklist

Auth:

```text
[ ] POST /api/v1/auth/register
[ ] POST /api/v1/auth/login
[ ] POST /api/v1/auth/refresh
```

Users:

```text
[ ] GET    /api/v1/users
[ ] GET    /api/v1/users/{userId}
[ ] PUT    /api/v1/users/{userId}
[ ] DELETE /api/v1/users/{userId}
[ ] GET    /api/v1/users/me
[ ] PUT    /api/v1/users/me
```

Projects:

```text
[ ] POST   /api/v1/projects
[ ] GET    /api/v1/projects
[ ] GET    /api/v1/projects/{projectId}
[ ] PUT    /api/v1/projects/{projectId}
[ ] DELETE /api/v1/projects/{projectId}
```

Tasks:

```text
[ ] POST   /api/v1/tasks
[ ] GET    /api/v1/tasks
[ ] GET    /api/v1/tasks/{taskId}
[ ] PUT    /api/v1/tasks/{taskId}
[ ] DELETE /api/v1/tasks/{taskId}
[ ] PATCH  /api/v1/tasks/{taskId}/status
```

Security:

```text
[ ] Admin can manage users
[ ] Manager cannot manage users
[ ] Manager can manage projects
[ ] Manager can manage tasks
[ ] Member cannot manage projects
[ ] Member cannot create/update/delete tasks
[ ] Member can list only assigned tasks
[ ] Member can view only assigned tasks
[ ] Member can update only assigned task status
[ ] Invalid/expired/missing token returns 401
[ ] Authenticated but unauthorized access returns 403
```

Task workflow:

```text
[ ] Task starts as TODO
[ ] TODO -> IN_PROGRESS works
[ ] IN_PROGRESS -> IN_REVIEW works
[ ] IN_REVIEW -> DONE works
[ ] TODO -> BLOCKED works
[ ] IN_PROGRESS -> BLOCKED works
[ ] IN_REVIEW -> BLOCKED works
[ ] TODO -> DONE fails
[ ] DONE -> any status fails
[ ] BLOCKED -> any status fails
```

Validation:

```text
[ ] Missing title fails
[ ] Missing priority fails
[ ] Missing assigneeId fails
[ ] Missing dueDate fails
[ ] Past dueDate fails
[ ] Invalid enum fails
[ ] Invalid pagination fails
```

Documentation and deployment:

```text
[ ] docker compose up --build starts app, Postgres, Redis
[ ] Swagger UI opens
[ ] OpenAPI JSON opens
[ ] README includes setup instructions
[ ] README includes caching strategy
[ ] README includes DB design decision
[ ] README includes future improvements
```

## 11. Expected Overall Result

If all checks above pass, the implementation covers the assignment's core requirements:

```text
JWT authentication
Refresh-token rotation
Role-based access control
Organization-scoped users/projects/tasks
Task CRUD
Status transition enforcement
Pagination and filtering
Redis task-list caching
Cache invalidation
Consistent error responses
Dockerized deployment
Swagger/OpenAPI documentation
Critical integration tests
```
