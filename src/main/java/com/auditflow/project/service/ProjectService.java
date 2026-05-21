package com.auditflow.project.service;

import com.auditflow.common.exception.ApiException;
import com.auditflow.finding.FindingRepository;
import com.auditflow.organization.Organization;
import com.auditflow.organization.OrganizationRepository;
import com.auditflow.project.Project;
import com.auditflow.project.ProjectAuditor;
import com.auditflow.project.ProjectAuditorRepository;
import com.auditflow.project.ProjectRepository;
import com.auditflow.project.ProjectStatus;
import com.auditflow.project.dto.AssignAuditorRequest;
import com.auditflow.project.dto.ProjectCreateRequest;
import com.auditflow.project.dto.ProjectResponse;
import com.auditflow.project.dto.ProjectUpdateRequest;
import com.auditflow.security.AccessControlService;
import com.auditflow.security.CurrentUserService;
import com.auditflow.user.Role;
import com.auditflow.user.User;
import com.auditflow.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectAuditorRepository projectAuditorRepository;
    private final FindingRepository findingRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final CurrentUserService currentUserService;
    private final AccessControlService accessControlService;

    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request) {
        accessControlService.requireCanCreateProject();

        Long organizationId = currentUserService.getCurrentOrganizationId();
        Long currentUserId = currentUserService.getCurrentUserId();

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Organization not found"));

        User createdBy = userRepository.findByIdAndOrganizationId(currentUserId, organizationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        Project project = Project.builder()
                .organization(organization)
                .createdBy(createdBy)
                .name(request.name().trim())
                .description(normalizeNullable(request.description()))
                .target(request.target().trim())
                .status(request.status() != null ? request.status() : ProjectStatus.DRAFT)
                .build();

        projectRepository.save(project);

        return toResponse(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listProjects() {
        Long organizationId = currentUserService.getCurrentOrganizationId();

        if (currentUserService.isAdmin()) {
            return projectRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId)
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        Long auditorId = currentUserService.getCurrentUserId();

        return projectAuditorRepository.findByAuditorId(auditorId)
                .stream()
                .map(ProjectAuditor::getProject)
                .filter(project -> project.getOrganization().getId().equals(organizationId))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId) {
        Project project = accessControlService.requireProjectAccess(projectId);
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request) {
        accessControlService.requireAdmin();

        Project project = accessControlService.requireProjectInCurrentOrganization(projectId);

        if (request.name() != null && !request.name().isBlank()) {
            project.setName(request.name().trim());
        }

        if (request.description() != null) {
            project.setDescription(normalizeNullable(request.description()));
        }

        if (request.target() != null && !request.target().isBlank()) {
            project.setTarget(request.target().trim());
        }

        if (request.status() != null) {
            project.setStatus(request.status());
        }

        projectRepository.save(project);

        return toResponse(project);
    }

    @Transactional
    public void deleteProject(Long projectId) {
        accessControlService.requireAdmin();

        Project project = accessControlService.requireProjectInCurrentOrganization(projectId);

        findingRepository.deleteByProject(project);
        projectAuditorRepository.deleteByProject(project);
        projectRepository.delete(project);
    }

    @Transactional
    public ProjectResponse assignAuditor(Long projectId, AssignAuditorRequest request) {
        accessControlService.requireCanAssignAuditor();

        Long organizationId = currentUserService.getCurrentOrganizationId();

        Project project = accessControlService.requireProjectInCurrentOrganization(projectId);

        User auditor = userRepository.findByIdAndOrganizationId(request.auditorId(), organizationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Auditor not found"));

        if (!Role.AUDITOR.equals(auditor.getRole())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "User must have AUDITOR role");
        }

        boolean alreadyAssigned = projectAuditorRepository.existsByProjectIdAndAuditorId(
                project.getId(),
                auditor.getId()
        );

        if (!alreadyAssigned) {
            ProjectAuditor projectAuditor = ProjectAuditor.builder()
                    .project(project)
                    .auditor(auditor)
                    .build();

            projectAuditorRepository.save(projectAuditor);
        }

        return toResponse(project);
    }

    private ProjectResponse toResponse(Project project) {
        List<ProjectAuditor> auditors = projectAuditorRepository.findByProjectId(project.getId());
        return ProjectResponse.from(project, auditors);
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}