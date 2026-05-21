package com.auditflow.auth.service;

import com.auditflow.auth.dto.AuthUserResponse;
import com.auditflow.auth.dto.LoginRequest;
import com.auditflow.auth.dto.RegisterRequest;
import com.auditflow.auth.security.UserPrincipal;
import com.auditflow.common.exception.ApiException;
import com.auditflow.organization.Organization;
import com.auditflow.organization.OrganizationRepository;
import com.auditflow.user.Role;
import com.auditflow.user.User;
import com.auditflow.user.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CookieService cookieService;

    @Transactional
    public AuthUserResponse register(RegisterRequest request, HttpServletResponse response) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }

        String organizationName = normalizeOrganizationName(request.organizationName(), request.fullName());

        Organization organization = organizationRepository.findByName(organizationName)
                .orElseGet(() -> organizationRepository.save(
                        Organization.builder()
                                .name(organizationName)
                                .description("Organization created during user registration.")
                                .active(true)
                                .build()
                ));

        User user = User.builder()
                .organization(organization)
                .fullName(request.fullName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.AUDITOR)
                .enabled(true)
                .build();

        userRepository.save(user);

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal);
        cookieService.addAuthCookie(response, token);

        return AuthUserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public AuthUserResponse login(LoginRequest request, HttpServletResponse response) {
        String email = normalizeEmail(request.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        String token = jwtService.generateToken(principal);
        cookieService.addAuthCookie(response, token);

        return AuthUserResponse.from(user);
    }

    public void logout(HttpServletResponse response) {
        cookieService.clearAuthCookie(response);
    }

    @Transactional(readOnly = true)
    public AuthUserResponse me(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user not found"));

        return AuthUserResponse.from(user);
    }

    private String normalizeEmail(String email) {
        return email.toLowerCase().trim();
    }

    private String normalizeOrganizationName(String organizationName, String fullName) {
        if (organizationName == null || organizationName.isBlank()) {
            return fullName.trim() + " Organization";
        }

        return organizationName.trim();
    }
}