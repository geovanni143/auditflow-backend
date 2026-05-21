package com.auditflow.seed;

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
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAuditorRepository projectAuditorRepository;
    private final FindingRepository findingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (organizationRepository.existsByName("N3X Security Lab")) {
            return;
        }

        Organization organization = Organization.builder()
                .name("N3X Security Lab")
                .description("Initial organization for AuditFlow demo environment.")
                .active(true)
                .build();

        organizationRepository.save(organization);

        User admin = User.builder()
                .organization(organization)
                .fullName("AuditFlow Admin")
                .email("admin@auditflow.local")
                .passwordHash(passwordEncoder.encode("Admin123!"))
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        User auditor = User.builder()
                .organization(organization)
                .fullName("AuditFlow Auditor")
                .email("auditor@auditflow.local")
                .passwordHash(passwordEncoder.encode("Auditor123!"))
                .role(Role.AUDITOR)
                .enabled(true)
                .build();

        userRepository.save(admin);
        userRepository.save(auditor);

        Project project = Project.builder()
                .organization(organization)
                .createdBy(admin)
                .name("Web Application Security Audit")
                .description("Initial audit project for testing AuditFlow domain model.")
                .target("https://example.com")
                .status(ProjectStatus.ACTIVE)
                .build();

        projectRepository.save(project);

        ProjectAuditor projectAuditor = ProjectAuditor.builder()
                .project(project)
                .auditor(auditor)
                .build();

        projectAuditorRepository.save(projectAuditor);

        Finding finding = Finding.builder()
                .project(project)
                .reportedBy(auditor)
                .title("Missing security headers")
                .description("The target application does not include recommended HTTP security headers.")
                .recommendation("Configure headers such as Content-Security-Policy, X-Frame-Options and X-Content-Type-Options.")
                .evidence("Security headers were not present in the HTTP response.")
                .severity(Severity.MEDIUM)
                .status(FindingStatus.OPEN)
                .build();

        findingRepository.save(finding);
    }
}