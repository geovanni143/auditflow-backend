package com.auditflow.project.dto;

import jakarta.validation.constraints.NotNull;

public record AssignAuditorRequest(

        @NotNull(message = "Auditor id is required")
        Long auditorId
) {
}