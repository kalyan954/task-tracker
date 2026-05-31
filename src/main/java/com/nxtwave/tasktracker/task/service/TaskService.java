package com.nxtwave.tasktracker.task.service;

import org.springframework.data.domain.Page;

import com.nxtwave.tasktracker.task.dto.CreateTaskRequest;
import com.nxtwave.tasktracker.task.dto.TaskFilterRequest;
import com.nxtwave.tasktracker.task.dto.TaskResponse;
import com.nxtwave.tasktracker.task.dto.UpdateTaskStatusRequest;


public interface TaskService {

    TaskResponse createTask(CreateTaskRequest request);

    TaskResponse updateTaskStatus(Long taskId, UpdateTaskStatusRequest request);

    Page<TaskResponse> getTasks(TaskFilterRequest filter,int page,int limit);
}