package com.nxtwave.tasktracker.task.controller;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nxtwave.tasktracker.common.enums.Priority;
import com.nxtwave.tasktracker.common.enums.TaskStatus;
import com.nxtwave.tasktracker.task.dto.CreateTaskRequest;
import com.nxtwave.tasktracker.task.dto.TaskFilterRequest;
import com.nxtwave.tasktracker.task.dto.TaskResponse;
import com.nxtwave.tasktracker.task.dto.UpdateTaskStatusRequest;
import com.nxtwave.tasktracker.task.service.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN','MANAGER')"
    )
    @CacheEvict(
        value = "tasks",
        allEntries = true
    )
    public ResponseEntity<TaskResponse> createTask(
            @Valid
            @RequestBody
            CreateTaskRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(taskService.createTask(request));
    }

    @PatchMapping("/{taskId}/status")
    @CacheEvict(
        value = "tasks",
        allEntries = true
    )
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long taskId,
            @Valid
            @RequestBody
            UpdateTaskStatusRequest request
    ) {

        return ResponseEntity.ok(taskService.updateTaskStatus(taskId, request));
    }

    @GetMapping
    @Cacheable(
        value = "tasks",
        key =
        "#filter.assigneeId + '_' + #page + '_' + #limit"
    )
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
