package com.auditflow.seed;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.auditflow.finding.Finding;
import com.auditflow.finding.FindingRepository;
import com.auditflow.finding.FindingStatus;
import com.auditflow.finding.Severity;
import com.auditflow.organization.Organization;
import com.auditflow.organization.OrganizationRepository;
import com.auditflow.project.Project;
import com.auditflow.project.ProjectAuditor;
import com.auditflow.project.ProjectAuditorRepository;
import com.auditflow.project.ProjectRepository;
import com.auditflow.project.ProjectStatus;
import com.auditflow.user.Role;
import com.auditflow.user.User;
import com.auditflow.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private static final String DEMO_ORGANIZATION_NAME = "N3X Security";
    private static final String DEMO_ADMIN_EMAIL = "admin@auditflow.local";
    private static final String DEMO_AUDITOR_EMAIL = "auditor@auditflow.local";

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAuditorRepository projectAuditorRepository;
    private final FindingRepository findingRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:false}")
    private boolean seedEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedEnabled) {
            return;
        }

        if (organizationRepository.existsByName(DEMO_ORGANIZATION_NAME)) {
            return;
        }

        if (userRepository.existsByEmail(DEMO_ADMIN_EMAIL)) {
            return;
        }

        if (userRepository.existsByEmail(DEMO_AUDITOR_EMAIL)) {
            return;
        }

        seedDemoData();
    }

    private void seedDemoData() {
        Organization organization = Organization.builder()
                .name(DEMO_ORGANIZATION_NAME)
                .description("Demo organization for AuditFlow technical evaluation.")
                .active(true)
                .build();

        organizationRepository.save(organization);

        User admin = User.builder()
                .organization(organization)
                .fullName("AuditFlow Admin")
                .email(DEMO_ADMIN_EMAIL)
                .passwordHash(passwordEncoder.encode("Admin123!"))
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        User auditor = User.builder()
                .organization(organization)
                .fullName("AuditFlow Auditor")
                .email(DEMO_AUDITOR_EMAIL)
                .passwordHash(passwordEncoder.encode("Auditor123!"))
                .role(Role.AUDITOR)
                .enabled(true)
                .build();

        userRepository.save(admin);
        userRepository.save(auditor);

        Project webAuditProject = Project.builder()
                .organization(organization)
                .createdBy(admin)
                .name("Web Application Security Audit")
                .description("Main demo project for testing RBAC, project assignment, findings and filters.")
                .target("Demo Client")
                .status(ProjectStatus.ACTIVE)
                .build();

        Project internalApiProject = Project.builder()
                .organization(organization)
                .createdBy(admin)
                .name("Internal API Review")
                .description("Internal API review project. This project is intentionally not assigned to the demo auditor.")
                .target("Internal Client")
                .status(ProjectStatus.IN_REVIEW)
                .build();

        projectRepository.save(webAuditProject);
        projectRepository.save(internalApiProject);

        ProjectAuditor projectAuditor = ProjectAuditor.builder()
                .project(webAuditProject)
                .auditor(auditor)
                .build();

        projectAuditorRepository.save(projectAuditor);

        findingRepository.save(Finding.builder()
                .project(webAuditProject)
                .reportedBy(auditor)
                .title("SQL Injection Risk")
                .description("A parameterized query is not enforced in one of the authentication-related endpoints.")
                .recommendation("Use parameterized queries, validate input and add automated regression tests.")
                .evidence("Payload example: ' OR '1'='1")
                .severity(Severity.CRITICAL)
                .status(FindingStatus.OPEN)
                .build());

        findingRepository.save(Finding.builder()
                .project(webAuditProject)
                .reportedBy(auditor)
                .title("Missing Rate Limiting")
                .description("The login endpoint does not enforce enough request throttling.")
                .recommendation("Add rate limiting per IP and per account identifier.")
                .evidence("Multiple login attempts were accepted without delay.")
                .severity(Severity.HIGH)
                .status(FindingStatus.IN_PROGRESS)
                .build());

        findingRepository.save(Finding.builder()
                .project(webAuditProject)
                .reportedBy(auditor)
                .title("Weak Password Policy")
                .description("The password policy allows weak passwords in some user creation flows.")
                .recommendation("Require minimum length, complexity and breached password checks.")
                .evidence("Weak passwords were accepted during validation testing.")
                .severity(Severity.MEDIUM)
                .status(FindingStatus.RESOLVED)
                .build());

        findingRepository.save(Finding.builder()
                .project(webAuditProject)
                .reportedBy(auditor)
                .title("Verbose Error Messages")
                .description("Some API responses expose implementation details in error messages.")
                .recommendation("Return generic client-facing errors and log technical details server-side.")
                .evidence("Stack-related information was visible in selected error responses.")
                .severity(Severity.LOW)
                .status(FindingStatus.CLOSED)
                .build());
    }
}