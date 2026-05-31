package com.nxtwave.tasktracker.task.service.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.nxtwave.tasktracker.common.enums.Role;
import com.nxtwave.tasktracker.common.enums.TaskStatus;
import com.nxtwave.tasktracker.common.exception.InvalidStatusTransitionException;
import com.nxtwave.tasktracker.common.exception.ResourceNotFoundException;
import com.nxtwave.tasktracker.common.exception.UnauthorizedException;
import com.nxtwave.tasktracker.common.security.CurrentUserUtil;
import com.nxtwave.tasktracker.task.dto.CreateTaskRequest;
import com.nxtwave.tasktracker.task.dto.TaskFilterRequest;
import com.nxtwave.tasktracker.task.dto.TaskResponse;
import com.nxtwave.tasktracker.task.dto.UpdateTaskRequest;
import com.nxtwave.tasktracker.task.dto.UpdateTaskStatusRequest;
import com.nxtwave.tasktracker.task.entity.Task;
import com.nxtwave.tasktracker.task.repository.TaskRepository;
import com.nxtwave.tasktracker.task.security.TaskAuthorizationService;
import com.nxtwave.tasktracker.task.service.TaskService;
import com.nxtwave.tasktracker.task.specification.TaskSpecification;
import com.nxtwave.tasktracker.task.validator.TaskStatusTransitionValidator;
import com.nxtwave.tasktracker.user.entity.User;
import com.nxtwave.tasktracker.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final TaskAuthorizationService taskAuthorizationService;

    @Transactional
    @Override
    @CacheEvict(
        value = "tasks",
        allEntries = true
    )
    public TaskResponse createTask(CreateTaskRequest request) {

        User assignee = userRepository.findById(request.getAssigneeId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Assignee not found"
                                )
                        );

        String email = CurrentUserUtil.getCurrentUserEmail();

        User currentUser = userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "User not found"
                                )
                        );

        if (!currentUser.getOrganization().getId().equals(assignee.getOrganization().getId())) {

            throw new UnauthorizedException(
                    "Cannot assign task outside organization"
            );
        }

        Task task = new Task();

        task.setTitle(request.getTitle());

        task.setDescription(request.getDescription());

        task.setPriority(request.getPriority());

        task.setStatus(TaskStatus.TODO);

        task.setDueDate(request.getDueDate());

        task.setAssignee(assignee);

        taskRepository.save(task);

        return mapToResponse(task);
    }

    @Transactional
    @Override
    @CacheEvict(
        value = "tasks",
        allEntries = true
    )
    public TaskResponse updateTaskStatus(Long taskId, UpdateTaskStatusRequest request) {

        Task task = taskRepository.findById(taskId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Task not found"
                                )
                        );

        String email = CurrentUserUtil.getCurrentUserEmail();

        User currentUser = userRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        taskAuthorizationService.validateOrganizationAccess(task, currentUser);

        taskAuthorizationService.validateTaskViewAndStatusUpdatePermission(
                        task,
                        currentUser
                );
    
        TaskStatus currentStatus = task.getStatus();

        TaskStatus newStatus = request.getStatus();


        boolean validTransition = TaskStatusTransitionValidator
                        .isValidTransition(currentStatus, newStatus);

        if (!validTransition) {

            throw new InvalidStatusTransitionException(
                    "Invalid status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }

        task.setStatus(newStatus);

        taskRepository.save(task);

        return mapToResponse(task);
    }

    @Override
    @Cacheable(
    value = "tasks", key =
    "#filter.assigneeId + '_' + #filter.status + '_' + #filter.priority + '_' + #page + '_' + #limit"
    )
    public Page<TaskResponse> getTasks(TaskFilterRequest filter, int page, int limit) {

        Pageable pageable = PageRequest.of(page, limit);

        String email = CurrentUserUtil.getCurrentUserEmail();

        User currentUser = userRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (currentUser.getRole() == Role.MEMBER) {
            filter.setAssigneeId(
                    currentUser.getId()
            );
        }

        Specification<Task> specification = TaskSpecification.withFilters(filter);

        return taskRepository.findAll(specification, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public TaskResponse getTaskById(Long taskId) {

        Task task = taskRepository.findById(taskId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Task not found"
                                )
                        );

        String email = CurrentUserUtil.getCurrentUserEmail();

        User currentUser = userRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        taskAuthorizationService.validateOrganizationAccess(task, currentUser);

        taskAuthorizationService.validateTaskViewAndStatusUpdatePermission(
                        task,
                        currentUser
                );

        return mapToResponse(task);
    }

    private TaskResponse mapToResponse(Task task) {

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .assigneeId(
                        task.getAssignee() != null
                                ? task.getAssignee().getId()
                                : null
                )
                .assigneeName(
                        task.getAssignee() != null
                                ? task.getAssignee().getName()
                                : null
                )
                .build();
    }

    @Transactional
    @Override
    @CacheEvict(
        value = "tasks",
        allEntries = true
    )
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request) {

        Task task = taskRepository.findById(taskId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Task not found"
                                )
                        );

        User assignee = userRepository.findById(
                                request.getAssigneeId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Assignee not found"
                                )
                        );

        String email = CurrentUserUtil.getCurrentUserEmail();

        User currentUser = userRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!currentUser.getOrganization().getId().equals(assignee.getOrganization().getId())) {

            throw new UnauthorizedException(
                    "Cannot assign task outside organization"
            );
        }

        taskAuthorizationService.validateOrganizationAccess(task, currentUser);

        task.setTitle(request.getTitle());

        task.setDescription(request.getDescription());

        task.setPriority(request.getPriority());

        task.setDueDate(request.getDueDate());

        task.setAssignee(assignee);

        taskRepository.save(task);

        return mapToResponse(task);
    }

    @Transactional
    @Override
    @CacheEvict(
        value = "tasks",
        allEntries = true
    )
    public void deleteTask(Long taskId) {

        Task task = taskRepository.findById(taskId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Task not found"
                                )
                        );

        String email = CurrentUserUtil.getCurrentUserEmail();

        User currentUser = userRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        taskAuthorizationService.validateOrganizationAccess(task, currentUser);

        taskRepository.delete(task);
    }
}
