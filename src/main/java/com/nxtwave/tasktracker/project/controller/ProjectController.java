package com.nxtwave.tasktracker.project.controller;

import com.nxtwave.tasktracker.project.dto.CreateProjectRequest;
import com.nxtwave.tasktracker.project.dto.ProjectResponse;
import com.nxtwave.tasktracker.project.dto.UpdateProjectRequest;
import com.nxtwave.tasktracker.project.service.ProjectService;
import com.nxtwave.tasktracker.task.dto.ApiSuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Projects", description = "Organization-scoped project management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create Project", description = "Create a project. Roles: ADMIN, MANAGER")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(projectService.createProject(request));
    }

    @GetMapping
    @Operation(summary = "List Projects", description = "List projects in the authenticated user's organization. Roles: ADMIN, MANAGER")
    public ResponseEntity<Page<ProjectResponse>> getProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {

        return ResponseEntity.ok(projectService.getProjects(page, limit));
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Get Project by ID", description = "Retrieve a project in the authenticated user's organization. Roles: ADMIN, MANAGER")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long projectId) {

        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "Update Project", description = "Update a project. Roles: ADMIN, MANAGER")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request
    ) {

        return ResponseEntity.ok(projectService.updateProject(projectId, request));
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "Delete Project", description = "Delete a project if no tasks are linked to it. Roles: ADMIN, MANAGER")
    public ResponseEntity<ApiSuccessResponse> deleteProject(@PathVariable Long projectId) {

        projectService.deleteProject(projectId);

        return ResponseEntity.ok(new ApiSuccessResponse("Project deleted successfully"));
    }
}
