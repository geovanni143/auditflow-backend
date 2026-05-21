package com.auditflow.project.dto;

import com.auditflow.project.ProjectStatus;
import jakarta.validation.constraints.Size;

public record ProjectUpdateRequest(

        @Size(min = 3, max = 160, message = "Project name must be between 3 and 160 characters")
        String name,

        @Size(max = 700, message = "Description must not exceed 700 characters")
        String description,

        @Size(min = 3, max = 200, message = "Target must be between 3 and 200 characters")
        String target,

        ProjectStatus status
) {
}