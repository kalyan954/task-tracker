package com.nxtwave.tasktracker.task.dto;

import com.nxtwave.tasktracker.common.enums.Priority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTaskRequest {

    // Fields are optional for partial updates. Validation for non-null values still applies where appropriate.
    private String title;

    private String description;

    private Priority priority;

    private Long assigneeId;

    @Future(message = "due_date must be a future date")
    private LocalDate dueDate;
}
