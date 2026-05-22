package com.auditflow.user;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<UserResponse> getUsers(Authentication authentication) {
        User currentAdmin = getCurrentAdmin(authentication);
        Long organizationId = currentAdmin.getOrganization().getId();

        return userRepository
                .findAll()
                .stream()
                .filter(user -> user.getOrganization() != null)
                .filter(user -> user.getOrganization().getId().equals(organizationId))
                .sorted(Comparator.comparing(User::getFullName))
                .map(UserResponse::from)
                .toList();
    }

    @GetMapping("/auditors")
    @Transactional(readOnly = true)
    public List<UserResponse> getAuditors(Authentication authentication) {
        User currentAdmin = getCurrentAdmin(authentication);
        Long organizationId = currentAdmin.getOrganization().getId();

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

    @PostMapping("/admins")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public UserResponse createAdmin(
            Authentication authentication,
            @Valid @RequestBody CreateUserRequest request
    ) {
        User currentAdmin = getCurrentAdmin(authentication);

        User createdUser = createUserInsideCurrentOrganization(
                currentAdmin,
                request,
                Role.ADMIN
        );

        return UserResponse.from(createdUser);
    }

    @PostMapping("/auditors")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public UserResponse createAuditor(
            Authentication authentication,
            @Valid @RequestBody CreateUserRequest request
    ) {
        User currentAdmin = getCurrentAdmin(authentication);

        User createdUser = createUserInsideCurrentOrganization(
                currentAdmin,
                request,
                Role.AUDITOR
        );

        return UserResponse.from(createdUser);
    }

    @PutMapping("/{userId}")
    @Transactional
    public UserResponse updateUser(
            Authentication authentication,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        User currentAdmin = getCurrentAdmin(authentication);
        User targetUser = getUserFromSameOrganization(currentAdmin, userId);

        targetUser.setFullName(request.fullName().trim());
        targetUser.setEnabled(request.enabled());

        User savedUser = userRepository.save(targetUser);

        return UserResponse.from(savedUser);
    }

    @DeleteMapping("/{userId}")
    @Transactional
    public UserResponse deactivateUser(
            Authentication authentication,
            @PathVariable Long userId
    ) {
        User currentAdmin = getCurrentAdmin(authentication);
        User targetUser = getUserFromSameOrganization(currentAdmin, userId);

        if (currentAdmin.getId().equals(targetUser.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No puedes desactivar tu propio usuario"
            );
        }

        targetUser.setEnabled(false);

        User savedUser = userRepository.save(targetUser);

        return UserResponse.from(savedUser);
    }

    private User getCurrentAdmin(Authentication authentication) {
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
                    "Only ADMIN users can manage users"
            );
        }

        return currentUser;
    }

    private User getUserFromSameOrganization(User currentAdmin, Long userId) {
        Long organizationId = currentAdmin.getOrganization().getId();

        User targetUser = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        if (
                targetUser.getOrganization() == null ||
                !targetUser.getOrganization().getId().equals(organizationId)
        ) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }

        return targetUser;
    }

    private User createUserInsideCurrentOrganization(
            User currentAdmin,
            CreateUserRequest request,
            Role role
    ) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo ya está registrado"
            );
        }

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(role)
                .enabled(true)
                .organization(currentAdmin.getOrganization())
                .build();

        return userRepository.save(user);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException exception
    ) {
        int statusCode = exception.getStatusCode().value();
        HttpStatus status = HttpStatus.valueOf(statusCode);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", statusCode);
        body.put("error", status.getReasonPhrase());
        body.put("message", exception.getReason());
        body.put("validationErrors", null);

        return ResponseEntity.status(status).body(body);
    }

    public record CreateUserRequest(
            @NotBlank(message = "El nombre completo es obligatorio")
            @Size(max = 120, message = "El nombre completo no puede superar 120 caracteres")
            String fullName,

            @NotBlank(message = "El correo es obligatorio")
            @Email(message = "El correo debe tener un formato válido")
            @Size(max = 160, message = "El correo no puede superar 160 caracteres")
            String email,

            @NotBlank(message = "La contraseña es obligatoria")
            @Size(min = 8, max = 120, message = "La contraseña debe tener entre 8 y 120 caracteres")
            String password
    ) {
    }

    public record UpdateUserRequest(
            @NotBlank(message = "El nombre completo es obligatorio")
            @Size(max = 120, message = "El nombre completo no puede superar 120 caracteres")
            String fullName,

            Boolean enabled
    ) {
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