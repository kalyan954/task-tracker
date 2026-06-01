package com.nxtwave.tasktracker.task.dto;

import com.nxtwave.tasktracker.common.enums.Priority;
import com.nxtwave.tasktracker.common.enums.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
public class TaskResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String description;

    private Priority priority;

    private TaskStatus status;

    private LocalDate dueDate;

    private Long assigneeId;

    private String assigneeName;

    private Long projectId;

    private String projectName;
}
