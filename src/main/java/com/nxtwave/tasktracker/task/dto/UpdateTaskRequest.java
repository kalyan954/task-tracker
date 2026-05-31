package com.nxtwave.tasktracker.task.dto;

import com.nxtwave.tasktracker.common.enums.Priority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTaskRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Priority priority;

    @NotNull
    private Long assigneeId;

    @NotNull
    @Future
    private LocalDate dueDate;
}