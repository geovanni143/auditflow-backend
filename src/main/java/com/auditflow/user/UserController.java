package com.auditflow.user;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/auditors")
    @Transactional(readOnly = true)
    public List<UserResponse> getAuditors(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication is required"
            );
        }

        User currentUser = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user not found"
                ));

        if (currentUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only ADMIN users can list auditors"
            );
        }

        Long organizationId = currentUser.getOrganization().getId();

        return userRepository
                .findAll()
                .stream()
                .filter(user -> user.getOrganization() != null)
                .filter(user -> user.getOrganization().getId().equals(organizationId))
                .filter(user -> user.getRole() == Role.AUDITOR)
                .sorted(Comparator.comparing(User::getFullName))
                .map(UserResponse::from)
                .toList();
    }

    public record UserResponse(
            Long id,
            String fullName,
            String email,
            Role role,
            Boolean enabled,
            Long organizationId,
            String organizationName
    ) {
        public static UserResponse from(User user) {
            return new UserResponse(
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
}