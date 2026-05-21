package com.auditflow.auth;

import com.auditflow.auth.dto.AuthUserResponse;
import com.auditflow.auth.dto.LoginRequest;
import com.auditflow.auth.dto.RegisterRequest;
import com.auditflow.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthUserResponse register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        return authService.register(request, response);
    }

    @PostMapping("/login")
    public AuthUserResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        return authService.login(request, response);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        authService.logout(response);
    }

    @GetMapping("/me")
    public AuthUserResponse me(Authentication authentication) {
        return authService.me(authentication.getName());
    }
}