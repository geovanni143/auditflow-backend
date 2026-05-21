package com.auditflow.security;

import com.auditflow.common.exception.ApiException;
import com.auditflow.finding.Finding;
import com.auditflow.finding.FindingRepository;
import com.auditflow.project.Project;
import com.auditflow.project.ProjectAuditorRepository;
import com.auditflow.project.ProjectRepository;
import com.auditflow.user.Role;
import com.auditflow.user.User;
import com.auditflow.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectAuditorRepository projectAuditorRepository;
    private final FindingRepository findingRepository;

    /*
     * RBAC: solo ADMIN.
     */
    public void requireAdmin() {
        if (!Role.ADMIN.equals(currentUserService.getCurrentRole())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role is required");
        }
    }

    /*
     * RBAC: ADMIN o AUDITOR.
     */
    public void requireAuthenticatedUser() {
        currentUserService.getCurrentPrincipal();
    }

    /*
     * Valida que el usuario objetivo exista dentro de la misma organización.
     * Si no existe o es de otra organización, respondemos 404 para evitar IDOR.
     */
    @Transactional(readOnly = true)
    public User requireUserInCurrentOrganization(Long userId) {
        Long organizationId = currentUserService.getCurrentOrganizationId();

        return userRepository.findByIdAndOrganizationId(userId, organizationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    /*
     * Valida que el proyecto exista dentro de la organización del usuario autenticado.
     * Si el proyecto pertenece a otra organización, devolvemos 404.
     */
    @Transactional(readOnly = true)
    public Project requireProjectInCurrentOrganization(Long projectId) {
        Long organizationId = currentUserService.getCurrentOrganizationId();

        return projectRepository.findByIdAndOrganizationId(projectId, organizationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found"));
    }

    /*
     * ADMIN puede acceder a cualquier proyecto de su organización.
     * AUDITOR solo puede acceder si está asignado al proyecto.
     */
    @Transactional(readOnly = true)
    public Project requireProjectAccess(Long projectId) {
        Project project = requireProjectInCurrentOrganization(projectId);

        if (currentUserService.isAdmin()) {
            return project;
        }

        Long currentUserId = currentUserService.getCurrentUserId();
        Long organizationId = currentUserService.getCurrentOrganizationId();

        boolean assigned = projectAuditorRepository.existsByProjectIdAndAuditorIdAndProjectOrganizationId(
                projectId,
                currentUserId,
                organizationId
        );

        if (!assigned) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not assigned to this project");
        }

        return project;
    }

    /*
     * Solo ADMIN puede crear proyectos para su organización.
     */
    public void requireCanCreateProject() {
        requireAdmin();
    }

    /*
     * ADMIN puede asignar auditores.
     */
    public void requireCanAssignAuditor() {
        requireAdmin();
    }

    /*
     * ADMIN puede gestionar usuarios de su organización.
     */
    public void requireCanManageUsers() {
        requireAdmin();
    }

    /*
     * Valida que el hallazgo exista dentro de la organización actual.
     * Si pertenece a otra organización, devolvemos 404 para evitar IDOR.
     */
    @Transactional(readOnly = true)
    public Finding requireFindingInCurrentOrganization(Long findingId) {
        Long organizationId = currentUserService.getCurrentOrganizationId();

        return findingRepository.findByIdAndProjectOrganizationId(findingId, organizationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Finding not found"));
    }

    /*
     * ADMIN puede acceder a cualquier hallazgo de su organización.
     * AUDITOR solo puede acceder a hallazgos de proyectos donde esté asignado.
     */
    @Transactional(readOnly = true)
    public Finding requireFindingAccess(Long findingId) {
        Finding finding = requireFindingInCurrentOrganization(findingId);

        if (currentUserService.isAdmin()) {
            return finding;
        }

        Long projectId = finding.getProject().getId();
        Long currentUserId = currentUserService.getCurrentUserId();
        Long organizationId = currentUserService.getCurrentOrganizationId();

        boolean assigned = projectAuditorRepository.existsByProjectIdAndAuditorIdAndProjectOrganizationId(
                projectId,
                currentUserId,
                organizationId
        );

        if (!assigned) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not assigned to this finding project");
        }

        return finding;
    }

    /*
     * Para crear hallazgos:
     * ADMIN puede crear hallazgos en proyectos de su organización.
     * AUDITOR solo puede crear hallazgos si está asignado al proyecto.
     */
    @Transactional(readOnly = true)
    public Project requireCanCreateFindingInProject(Long projectId) {
        return requireProjectAccess(projectId);
    }

    /*
     * Para editar hallazgos:
     * ADMIN puede editar cualquier hallazgo de su organización.
     * AUDITOR solo puede editar hallazgos de proyectos asignados.
     */
    @Transactional(readOnly = true)
    public Finding requireCanEditFinding(Long findingId) {
        return requireFindingAccess(findingId);
    }

    /*
     * Para cerrar hallazgos:
     * Por ahora permitimos ADMIN o AUDITOR asignado.
     * Si después quieres hacerlo más estricto, podemos dejar cerrar solo a ADMIN.
     */
    @Transactional(readOnly = true)
    public Finding requireCanCloseFinding(Long findingId) {
        return requireFindingAccess(findingId);
    }
}