package com.auditflow.project.dto;

import com.auditflow.user.Role;
import com.auditflow.user.User;

public record ProjectAuditorResponse(
        Long id,
        String fullName,
        String email,
        Role role
) {

    public static ProjectAuditorResponse from(User user) {
        return new ProjectAuditorResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }
}