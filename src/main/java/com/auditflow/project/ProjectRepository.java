package com.auditflow.project;

import com.auditflow.organization.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOrganization(Organization organization);

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByOrganizationAndStatus(Organization organization, ProjectStatus status);
}