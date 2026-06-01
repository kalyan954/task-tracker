package com.nxtwave.tasktracker.project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProjectRequest {

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;
}
