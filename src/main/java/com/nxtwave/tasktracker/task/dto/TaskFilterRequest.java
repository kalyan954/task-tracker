package com.nxtwave.tasktracker.task.dto;

import com.nxtwave.tasktracker.common.enums.Priority;
import com.nxtwave.tasktracker.common.enums.TaskStatus;
import lombok.Data;

@Data
public class TaskFilterRequest {

    private TaskStatus status;

    private Priority priority;

    private Long assigneeId;
}