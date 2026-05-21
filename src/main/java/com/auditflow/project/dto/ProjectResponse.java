package com.auditflow.project.dto;

import com.auditflow.project.Project;
import com.auditflow.project.ProjectAuditor;
import com.auditflow.project.ProjectStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectResponse(
        Long id,
        Long organizationId,
        String organizationName,
        Long createdById,
        String createdByName,
        String name,
        String description,
        String target,
        ProjectStatus status,
        List<ProjectAuditorResponse> auditors,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ProjectResponse from(Project project, List<ProjectAuditor> projectAuditors) {
        List<ProjectAuditorResponse> auditors = projectAuditors.stream()
                .map(projectAuditor -> ProjectAuditorResponse.from(projectAuditor.getAuditor()))
                .toList();

        return new ProjectResponse(
                project.getId(),
                project.getOrganization().getId(),
                project.getOrganization().getName(),
                project.getCreatedBy().getId(),
                project.getCreatedBy().getFullName(),
                project.getName(),
                project.getDescription(),
                project.getTarget(),
                project.getStatus(),
                auditors,
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}