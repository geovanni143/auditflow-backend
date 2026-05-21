package com.auditflow.finding;

import com.auditflow.common.PageResponse;
import com.auditflow.finding.dto.FindingCreateRequest;
import com.auditflow.finding.dto.FindingResponse;
import com.auditflow.finding.dto.FindingUpdateRequest;
import com.auditflow.finding.service.FindingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/findings")
@RequiredArgsConstructor
public class FindingController {

    private final FindingService findingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FindingResponse createFinding(@Valid @RequestBody FindingCreateRequest request) {
        return findingService.createFinding(request);
    }

    @GetMapping
    public PageResponse<FindingResponse> listFindings(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Severity severity,
            @RequestParam(required = false) FindingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return findingService.listFindings(projectId, severity, status, page, size);
    }

    @GetMapping("/{findingId}")
    public FindingResponse getFindingById(@PathVariable Long findingId) {
        return findingService.getFindingById(findingId);
    }

    @PutMapping("/{findingId}")
    public FindingResponse updateFinding(
            @PathVariable Long findingId,
            @Valid @RequestBody FindingUpdateRequest request
    ) {
        return findingService.updateFinding(findingId, request);
    }

    @DeleteMapping("/{findingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFinding(@PathVariable Long findingId) {
        findingService.deleteFinding(findingId);
    }
}