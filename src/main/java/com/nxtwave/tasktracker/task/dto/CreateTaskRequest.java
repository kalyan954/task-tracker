package com.nxtwave.tasktracker.task.dto;

import java.time.LocalDate;

import com.nxtwave.tasktracker.common.enums.Priority;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateTaskRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Priority priority;

    @NotNull
    private Long assigneeId;

    @NotNull
    private LocalDate dueDate;
}


