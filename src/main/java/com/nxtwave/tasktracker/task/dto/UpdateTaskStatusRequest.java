package com.nxtwave.tasktracker.task.dto;

import com.nxtwave.tasktracker.common.enums.TaskStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateTaskStatusRequest {

    @NotNull
    private TaskStatus status;
}
