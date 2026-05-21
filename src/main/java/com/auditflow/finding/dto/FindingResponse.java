package com.auditflow.finding.dto;

import com.auditflow.finding.Finding;
import com.auditflow.finding.FindingStatus;
import com.auditflow.finding.Severity;

import java.time.LocalDateTime;

public record FindingResponse(
        Long id,
        Long projectId,
        String projectName,
        Long organizationId,
        Long reportedById,
        String reportedByName,
        String title,
        String description,
        String recommendation,
        String evidence,
        Severity severity,
        FindingStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static FindingResponse from(Finding finding) {
        return new FindingResponse(
                finding.getId(),
                finding.getProject().getId(),
                finding.getProject().getName(),
                finding.getProject().getOrganization().getId(),
                finding.getReportedBy().getId(),
                finding.getReportedBy().getFullName(),
                finding.getTitle(),
                finding.getDescription(),
                finding.getRecommendation(),
                finding.getEvidence(),
                finding.getSeverity(),
                finding.getStatus(),
                finding.getCreatedAt(),
                finding.getUpdatedAt()
        );
    }
}