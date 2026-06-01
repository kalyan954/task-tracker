package com.nxtwave.tasktracker.project.service.impl;

import com.nxtwave.tasktracker.common.exception.ResourceNotFoundException;
import com.nxtwave.tasktracker.common.exception.UnauthorizedException;
import com.nxtwave.tasktracker.common.security.CurrentUserUtil;
import com.nxtwave.tasktracker.project.dto.CreateProjectRequest;
import com.nxtwave.tasktracker.project.dto.ProjectResponse;
import com.nxtwave.tasktracker.project.dto.UpdateProjectRequest;
import com.nxtwave.tasktracker.project.entity.Project;
import com.nxtwave.tasktracker.project.repository.ProjectRepository;
import com.nxtwave.tasktracker.project.service.ProjectService;
import com.nxtwave.tasktracker.task.repository.TaskRepository;
import com.nxtwave.tasktracker.user.entity.User;
import com.nxtwave.tasktracker.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;

    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {

        User currentUser = getCurrentAuthenticatedUser();

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOrganization(currentUser.getOrganization());
        project.setCreatedBy(currentUser);

        projectRepository.save(project);

        return mapToResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponse> getProjects(int page, int limit) {

        validatePagination(page, limit);

        User currentUser = getCurrentAuthenticatedUser();
        Pageable pageable = PageRequest.of(page, limit);

        return projectRepository
                .findByOrganizationId(currentUser.getOrganization().getId(), pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId) {

        Project project = getProjectOrThrow(projectId);

        validateOrganizationAccess(project, getCurrentAuthenticatedUser());

        return mapToResponse(project);
    }

    @Override
    @Transactional
    @CacheEvict(value = "taskLists", allEntries = true)
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request) {

        Project project = getProjectOrThrow(projectId);

        validateOrganizationAccess(project, getCurrentAuthenticatedUser());

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        projectRepository.save(project);

        return mapToResponse(project);
    }

    @Override
    @Transactional
    @CacheEvict(value = "taskLists", allEntries = true)
    public void deleteProject(Long projectId) {

        Project project = getProjectOrThrow(projectId);

        validateOrganizationAccess(project, getCurrentAuthenticatedUser());

        if (taskRepository.existsByProjectId(projectId)) {
            throw new IllegalArgumentException("Project cannot be deleted while tasks are linked to it");
        }

        projectRepository.delete(project);
    }

    private Project getProjectOrThrow(Long projectId) {

        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    private void validateOrganizationAccess(Project project, User currentUser) {

        Long projectOrganizationId = project.getOrganization().getId();
        Long currentOrganizationId = currentUser.getOrganization().getId();

        if (!projectOrganizationId.equals(currentOrganizationId)) {
            throw new UnauthorizedException("You cannot access projects from another organization");
        }
    }

    private User getCurrentAuthenticatedUser() {

        String email = CurrentUserUtil.getCurrentUserEmail();

        if (email == null) {
            throw new UnauthorizedException("Authentication is required");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void validatePagination(int page, int limit) {

        if (page < 0) {
            throw new IllegalArgumentException("page must be zero or greater");
        }

        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("limit must be between 1 and 100");
        }
    }

    private ProjectResponse mapToResponse(Project project) {

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .organizationId(project.getOrganization().getId())
                .createdById(project.getCreatedBy().getId())
                .createdByName(project.getCreatedBy().getName())
                .build();
    }
}
