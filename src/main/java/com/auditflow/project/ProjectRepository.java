package com.auditflow.project;

import com.auditflow.organization.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOrganization(Organization organization);

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByOrganizationAndStatus(Organization organization, ProjectStatus status);

    Optional<Project> findByIdAndOrganizationId(Long id, Long organizationId);

    boolean existsByIdAndOrganizationId(Long id, Long organizationId);

    List<Project> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
}