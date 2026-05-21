package com.auditflow.project;

import com.auditflow.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectAuditorRepository extends JpaRepository<ProjectAuditor, Long> {

    List<ProjectAuditor> findByProject(Project project);

    List<ProjectAuditor> findByAuditor(User auditor);

    boolean existsByProjectAndAuditor(Project project, User auditor);

    boolean existsByProjectIdAndAuditorId(Long projectId, Long auditorId);

    boolean existsByProjectIdAndAuditorIdAndProjectOrganizationId(
            Long projectId,
            Long auditorId,
            Long organizationId
    );
}