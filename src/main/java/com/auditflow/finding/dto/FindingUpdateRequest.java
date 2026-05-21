package com.auditflow.finding.dto;

import com.auditflow.finding.FindingStatus;
import com.auditflow.finding.Severity;
import jakarta.validation.constraints.Size;

public record FindingUpdateRequest(

        @Size(min = 3, max = 180, message = "Title must be between 3 and 180 characters")
        String title,

        @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
        String description,

        @Size(max = 2000, message = "Recommendation must not exceed 2000 characters")
        String recommendation,

        @Size(max = 1000, message = "Evidence must not exceed 1000 characters")
        String evidence,

        Severity severity,

        FindingStatus status
) {
}