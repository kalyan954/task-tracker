package com.nxtwave.tasktracker.project.service;

import com.nxtwave.tasktracker.project.dto.CreateProjectRequest;
import com.nxtwave.tasktracker.project.dto.ProjectResponse;
import com.nxtwave.tasktracker.project.dto.UpdateProjectRequest;
import org.springframework.data.domain.Page;

public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request);

    Page<ProjectResponse> getProjects(int page, int limit);

    ProjectResponse getProjectById(Long projectId);

    ProjectResponse updateProject(Long projectId, UpdateProjectRequest request);

    void deleteProject(Long projectId);
}
