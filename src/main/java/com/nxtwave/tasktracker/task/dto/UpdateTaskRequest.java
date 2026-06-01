package com.nxtwave.tasktracker.task.dto;

import com.nxtwave.tasktracker.common.enums.Priority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTaskRequest {

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
