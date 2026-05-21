package com.auditflow.finding;

import com.auditflow.project.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FindingRepository extends JpaRepository<Finding, Long> {

    Optional<Finding> findByIdAndProjectOrganizationId(Long id, Long organizationId);

    boolean existsByIdAndProjectOrganizationId(Long id, Long organizationId);

    Page<Finding> findByProjectOrganizationId(Long organizationId, Pageable pageable);

    Page<Finding> findByProjectOrganizationIdAndSeverity(
            Long organizationId,
            Severity severity,
            Pageable pageable
    );

    Page<Finding> findByProjectOrganizationIdAndStatus(
            Long organizationId,
            FindingStatus status,
            Pageable pageable
    );

    Page<Finding> findByProjectOrganizationIdAndSeverityAndStatus(
            Long organizationId,
            Severity severity,
            FindingStatus status,
            Pageable pageable
    );

    Page<Finding> findByProjectIdAndProjectOrganizationId(
            Long projectId,
            Long organizationId,
            Pageable pageable
    );

    Page<Finding> findByProjectIdAndProjectOrganizationIdAndSeverity(
            Long projectId,
            Long organizationId,
            Severity severity,
            Pageable pageable
    );

    Page<Finding> findByProjectIdAndProjectOrganizationIdAndStatus(
            Long projectId,
            Long organizationId,
            FindingStatus status,
            Pageable pageable
    );

    Page<Finding> findByProjectIdAndProjectOrganizationIdAndSeverityAndStatus(
            Long projectId,
            Long organizationId,
            Severity severity,
            FindingStatus status,
            Pageable pageable
    );

    Page<Finding> findByProjectIdIn(
            java.util.List<Long> projectIds,
            Pageable pageable
    );

    Page<Finding> findByProjectIdInAndSeverity(
            java.util.List<Long> projectIds,
            Severity severity,
            Pageable pageable
    );

    Page<Finding> findByProjectIdInAndStatus(
            java.util.List<Long> projectIds,
            FindingStatus status,
            Pageable pageable
    );

    Page<Finding> findByProjectIdInAndSeverityAndStatus(
            java.util.List<Long> projectIds,
            Severity severity,
            FindingStatus status,
            Pageable pageable
    );

    void deleteByProject(Project project);
}