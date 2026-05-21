package com.auditflow.project;

import com.auditflow.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectAuditorRepository extends JpaRepository<ProjectAuditor, Long> {

    List<ProjectAuditor> findByProject(Project project);

    List<ProjectAuditor> findByAuditor(User auditor);

    List<ProjectAuditor> findByAuditorId(Long auditorId);

    List<ProjectAuditor> findByProjectId(Long projectId);

    Optional<ProjectAuditor> findByProjectIdAndAuditorId(Long projectId, Long auditorId);

    boolean existsByProjectAndAuditor(Project project, User auditor);

    boolean existsByProjectIdAndAuditorId(Long projectId, Long auditorId);

    boolean existsByProjectIdAndAuditorIdAndProjectOrganizationId(
            Long projectId,
            Long auditorId,
            Long organizationId
    );

    void deleteByProject(Project project);
}