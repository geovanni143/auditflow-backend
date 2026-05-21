package com.auditflow.finding.service;

import com.auditflow.common.PageResponse;
import com.auditflow.common.exception.ApiException;
import com.auditflow.finding.Finding;
import com.auditflow.finding.FindingRepository;
import com.auditflow.finding.FindingStatus;
import com.auditflow.finding.Severity;
import com.auditflow.finding.dto.FindingCreateRequest;
import com.auditflow.finding.dto.FindingResponse;
import com.auditflow.finding.dto.FindingUpdateRequest;
import com.auditflow.project.Project;
import com.auditflow.project.ProjectAuditor;
import com.auditflow.project.ProjectAuditorRepository;
import com.auditflow.security.AccessControlService;
import com.auditflow.security.CurrentUserService;
import com.auditflow.user.User;
import com.auditflow.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindingService {

    private final FindingRepository findingRepository;
    private final UserRepository userRepository;
    private final ProjectAuditorRepository projectAuditorRepository;
    private final CurrentUserService currentUserService;
    private final AccessControlService accessControlService;

    @Transactional
    public FindingResponse createFinding(FindingCreateRequest request) {
        Project project = accessControlService.requireCanCreateFindingInProject(request.projectId());

        Long organizationId = currentUserService.getCurrentOrganizationId();
        Long currentUserId = currentUserService.getCurrentUserId();

        User reportedBy = userRepository.findByIdAndOrganizationId(currentUserId, organizationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        Finding finding = Finding.builder()
                .project(project)
                .reportedBy(reportedBy)
                .title(request.title().trim())
                .description(request.description().trim())
                .recommendation(normalizeNullable(request.recommendation()))
                .evidence(normalizeNullable(request.evidence()))
                .severity(request.severity())
                .status(request.status() != null ? request.status() : FindingStatus.OPEN)
                .build();

        findingRepository.save(finding);

        return FindingResponse.from(finding);
    }

    @Transactional(readOnly = true)
    public PageResponse<FindingResponse> listFindings(
            Long projectId,
            Severity severity,
            FindingStatus status,
            int page,
            int size
    ) {
        Pageable pageable = buildPageable(page, size);

        Page<Finding> findings;

        if (currentUserService.isAdmin()) {
            findings = listFindingsForAdmin(projectId, severity, status, pageable);
        } else {
            findings = listFindingsForAuditor(projectId, severity, status, pageable);
        }

        Page<FindingResponse> responsePage = findings.map(FindingResponse::from);

        return PageResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public FindingResponse getFindingById(Long findingId) {
        Finding finding = accessControlService.requireFindingAccess(findingId);
        return FindingResponse.from(finding);
    }

    @Transactional
    public FindingResponse updateFinding(Long findingId, FindingUpdateRequest request) {
        Finding finding = accessControlService.requireCanEditFinding(findingId);

        if (request.title() != null && !request.title().isBlank()) {
            finding.setTitle(request.title().trim());
        }

        if (request.description() != null && !request.description().isBlank()) {
            finding.setDescription(request.description().trim());
        }

        if (request.recommendation() != null) {
            finding.setRecommendation(normalizeNullable(request.recommendation()));
        }

        if (request.evidence() != null) {
            finding.setEvidence(normalizeNullable(request.evidence()));
        }

        if (request.severity() != null) {
            finding.setSeverity(request.severity());
        }

        if (request.status() != null) {
            finding.setStatus(request.status());
        }

        findingRepository.save(finding);

        return FindingResponse.from(finding);
    }

    @Transactional
    public void deleteFinding(Long findingId) {
        Finding finding = accessControlService.requireFindingAccess(findingId);
        findingRepository.delete(finding);
    }

    private Page<Finding> listFindingsForAdmin(
            Long projectId,
            Severity severity,
            FindingStatus status,
            Pageable pageable
    ) {
        Long organizationId = currentUserService.getCurrentOrganizationId();

        if (projectId != null) {
            accessControlService.requireProjectInCurrentOrganization(projectId);

            if (severity != null && status != null) {
                return findingRepository.findByProjectIdAndProjectOrganizationIdAndSeverityAndStatus(
                        projectId,
                        organizationId,
                        severity,
                        status,
                        pageable
                );
            }

            if (severity != null) {
                return findingRepository.findByProjectIdAndProjectOrganizationIdAndSeverity(
                        projectId,
                        organizationId,
                        severity,
                        pageable
                );
            }

            if (status != null) {
                return findingRepository.findByProjectIdAndProjectOrganizationIdAndStatus(
                        projectId,
                        organizationId,
                        status,
                        pageable
                );
            }

            return findingRepository.findByProjectIdAndProjectOrganizationId(
                    projectId,
                    organizationId,
                    pageable
            );
        }

        if (severity != null && status != null) {
            return findingRepository.findByProjectOrganizationIdAndSeverityAndStatus(
                    organizationId,
                    severity,
                    status,
                    pageable
            );
        }

        if (severity != null) {
            return findingRepository.findByProjectOrganizationIdAndSeverity(
                    organizationId,
                    severity,
                    pageable
            );
        }

        if (status != null) {
            return findingRepository.findByProjectOrganizationIdAndStatus(
                    organizationId,
                    status,
                    pageable
            );
        }

        return findingRepository.findByProjectOrganizationId(organizationId, pageable);
    }

    private Page<Finding> listFindingsForAuditor(
            Long projectId,
            Severity severity,
            FindingStatus status,
            Pageable pageable
    ) {
        Long auditorId = currentUserService.getCurrentUserId();

        if (projectId != null) {
            accessControlService.requireProjectAccess(projectId);

            List<Long> projectIds = List.of(projectId);

            return listByProjectIds(projectIds, severity, status, pageable);
        }

        List<Long> assignedProjectIds = projectAuditorRepository.findByAuditorId(auditorId)
                .stream()
                .map(ProjectAuditor::getProject)
                .map(Project::getId)
                .toList();

        if (assignedProjectIds.isEmpty()) {
            return Page.empty(pageable);
        }

        return listByProjectIds(assignedProjectIds, severity, status, pageable);
    }

    private Page<Finding> listByProjectIds(
            List<Long> projectIds,
            Severity severity,
            FindingStatus status,
            Pageable pageable
    ) {
        if (severity != null && status != null) {
            return findingRepository.findByProjectIdInAndSeverityAndStatus(
                    projectIds,
                    severity,
                    status,
                    pageable
            );
        }

        if (severity != null) {
            return findingRepository.findByProjectIdInAndSeverity(
                    projectIds,
                    severity,
                    pageable
            );
        }

        if (status != null) {
            return findingRepository.findByProjectIdInAndStatus(
                    projectIds,
                    status,
                    pageable
            );
        }

        return findingRepository.findByProjectIdIn(projectIds, pageable);
    }

    private Pageable buildPageable(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        return PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}