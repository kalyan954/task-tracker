package com.nxtwave.tasktracker.project.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProjectResponse {

    private Long id;

    private String name;

    private String description;

    private Long organizationId;

    private Long createdById;

    private String createdByName;
}
