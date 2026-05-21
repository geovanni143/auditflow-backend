package com.auditflow.security;

import com.auditflow.auth.security.UserPrincipal;
import com.auditflow.common.exception.ApiException;
import com.auditflow.user.Role;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public UserPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserPrincipal userPrincipal)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        return userPrincipal;
    }

    public Long getCurrentUserId() {
        return getCurrentPrincipal().getId();
    }

    public Long getCurrentOrganizationId() {
        return getCurrentPrincipal().getOrganizationId();
    }

    public String getCurrentEmail() {
        return getCurrentPrincipal().getEmail();
    }

    public Role getCurrentRole() {
        return getCurrentPrincipal().getRole();
    }

    public boolean isAdmin() {
        return Role.ADMIN.equals(getCurrentRole());
    }

    public boolean isAuditor() {
        return Role.AUDITOR.equals(getCurrentRole());
    }
}