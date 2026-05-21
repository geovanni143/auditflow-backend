package com.auditflow.project;

import com.auditflow.project.dto.AssignAuditorRequest;
import com.auditflow.project.dto.ProjectCreateRequest;
import com.auditflow.project.dto.ProjectResponse;
import com.auditflow.project.dto.ProjectUpdateRequest;
import com.auditflow.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@Valid @RequestBody ProjectCreateRequest request) {
        return projectService.createProject(request);
    }

    @GetMapping
    public List<ProjectResponse> listProjects() {
        return projectService.listProjects();
    }

    @GetMapping("/{projectId}")
    public ProjectResponse getProjectById(@PathVariable Long projectId) {
        return projectService.getProjectById(projectId);
    }

    @PutMapping("/{projectId}")
    public ProjectResponse updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectUpdateRequest request
    ) {
        return projectService.updateProject(projectId, request);
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
    }

    @PostMapping("/{projectId}/auditors")
    public ProjectResponse assignAuditor(
            @PathVariable Long projectId,
            @Valid @RequestBody AssignAuditorRequest request
    ) {
        return projectService.assignAuditor(projectId, request);
    }
}