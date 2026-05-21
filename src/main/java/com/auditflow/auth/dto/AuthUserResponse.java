package com.auditflow.auth.dto;

import com.auditflow.user.Role;
import com.auditflow.user.User;

public record AuthUserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        Boolean enabled,
        Long organizationId,
        String organizationName
) {

    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getEnabled(),
                user.getOrganization().getId(),
                user.getOrganization().getName()
        );
    }
}