package com.nxtwave.tasktracker.task.controller;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nxtwave.tasktracker.common.enums.Priority;
import com.nxtwave.tasktracker.common.enums.TaskStatus;
import com.nxtwave.tasktracker.task.dto.ApiSuccessResponse;
import com.nxtwave.tasktracker.task.dto.CreateTaskRequest;
import com.nxtwave.tasktracker.task.dto.TaskFilterRequest;
import com.nxtwave.tasktracker.task.dto.TaskResponse;
import com.nxtwave.tasktracker.task.dto.UpdateTaskRequest;
import com.nxtwave.tasktracker.task.dto.UpdateTaskStatusRequest;
import com.nxtwave.tasktracker.task.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@Tag(name = "Tasks", description = "Endpoints for creating, viewing and managing tasks")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER')"
    )
        @Operation(summary = "Create Task", description = "Create a new task. Roles: ADMIN, MANAGER")
        @ApiResponses({
                @ApiResponse(responseCode = "201", description = "Task created",
                        content = @Content(schema = @Schema(implementation = TaskResponse.class))
                ),
                @ApiResponse(responseCode = "400", description = "Validation error"),
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "403", description = "Forbidden")
        })
    public ResponseEntity<TaskResponse> createTask(
            @Valid
            @RequestBody
            CreateTaskRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.createTask(request));
    }

    
    @GetMapping("/{taskId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER','MEMBER')"
    )
        @Operation(summary = "Get Task by ID", description = "Retrieve a task by its id. Roles: ADMIN, MANAGER, MEMBER")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Task returned",
                        content = @Content(schema = @Schema(implementation = TaskResponse.class))
                ),
                @ApiResponse(responseCode = "404", description = "Task not found"),
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "403", description = "Forbidden")
        })
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long taskId) {

        return ResponseEntity.ok(
                taskService.getTaskById(taskId)
        );
    }

    @PutMapping("/{taskId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER')"
    )
        @Operation(summary = "Update Task", description = "Update task details. Roles: ADMIN, MANAGER")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Task updated",
                        content = @Content(schema = @Schema(implementation = TaskResponse.class))
                ),
                @ApiResponse(responseCode = "400", description = "Validation error"),
                @ApiResponse(responseCode = "404", description = "Task not found"),
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "403", description = "Forbidden")
        })
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request
    ) {

        return ResponseEntity.ok(
                taskService.updateTask(
                        taskId,
                        request
                )
        );
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER')"
    )
        @Operation(summary = "Delete Task", description = "Delete a task by id. Roles: ADMIN, MANAGER")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Task deleted",
                        content = @Content(schema = @Schema(implementation = ApiSuccessResponse.class))
                ),
                @ApiResponse(responseCode = "404", description = "Task not found"),
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "403", description = "Forbidden")
        })
    public ResponseEntity<ApiSuccessResponse> deleteTask(
            @PathVariable Long taskId
    ) {

        taskService.deleteTask(taskId);

        return ResponseEntity.ok(
            new ApiSuccessResponse("Task deleted successfully")
        );
    }


    @PatchMapping("/{taskId}/status")
    @PreAuthorize(
        "hasAnyRole('ADMIN','MANAGER','MEMBER')"
    )
        @Operation(summary = "Update Task Status", description = "Update only the status of a task. Roles: ADMIN, MANAGER, MEMBER")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Task status updated",
                        content = @Content(schema = @Schema(implementation = TaskResponse.class))
                ),
                @ApiResponse(responseCode = "400", description = "Validation error"),
                @ApiResponse(responseCode = "404", description = "Task not found"),
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "403", description = "Forbidden")
        })
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long taskId,
            @Valid
            @RequestBody
            UpdateTaskStatusRequest request
    ) {

        return ResponseEntity.ok(taskService.updateTaskStatus(taskId, request));
    }

    @GetMapping
    @PreAuthorize(
        "hasAnyRole('ADMIN','MANAGER','MEMBER')"
    )
        @Operation(summary = "List Tasks", description = "Get paginated list of tasks with optional filters. Roles: ADMIN, MANAGER, MEMBER")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Page of tasks returned",
                        content = @Content(schema = @Schema(implementation = TaskResponse.class))
                ),
                @ApiResponse(responseCode = "401", description = "Unauthorized"),
                @ApiResponse(responseCode = "403", description = "Forbidden")
        })
    public ResponseEntity<Page<TaskResponse>>getTasks(
            @RequestParam(required = false) TaskStatus status, 
            @RequestParam(required = false)Priority priority,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {

        TaskFilterRequest filter = new TaskFilterRequest();

        filter.setStatus(status);

        filter.setPriority(priority);

        filter.setAssigneeId(assigneeId);

        return ResponseEntity.ok(taskService.getTasks(filter, page, limit));
    }
}
