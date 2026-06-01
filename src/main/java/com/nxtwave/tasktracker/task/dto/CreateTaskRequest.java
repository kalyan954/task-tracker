package com.nxtwave.tasktracker.task.dto;

import java.time.LocalDate;

import com.nxtwave.tasktracker.common.enums.Priority;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Priority is required")
    private Priority priority;

    @NotNull(message = "Assignee id is required")
    private Long assigneeId;

    @NotNull(message = "Due date is required")
    @Future(message = "due_date must be a future date")
    private LocalDate dueDate;
}


