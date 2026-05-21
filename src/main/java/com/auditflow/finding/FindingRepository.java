package com.auditflow.finding;

import com.auditflow.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FindingRepository extends JpaRepository<Finding, Long> {

    List<Finding> findByProject(Project project);

    List<Finding> findBySeverity(Severity severity);

    List<Finding> findByStatus(FindingStatus status);

    List<Finding> findByProjectAndStatus(Project project, FindingStatus status);

    long countBySeverity(Severity severity);

    long countByStatus(FindingStatus status);

    Optional<Finding> findByIdAndProjectOrganizationId(Long id, Long organizationId);

    boolean existsByIdAndProjectOrganizationId(Long id, Long organizationId);

    void deleteByProject(Project project);
}