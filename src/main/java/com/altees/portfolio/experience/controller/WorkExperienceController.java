package com.altees.portfolio.experience.controller;

import com.altees.portfolio.common.response.ApiResponse;
import com.altees.portfolio.experience.dto.WorkExperienceRequest;
import com.altees.portfolio.experience.dto.WorkExperienceResponse;
import com.altees.portfolio.experience.service.WorkExperienceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/experiences")
public class WorkExperienceController {

    private final WorkExperienceService workExperienceService;

    public WorkExperienceController(WorkExperienceService workExperienceService) {
        this.workExperienceService = workExperienceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkExperienceResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(workExperienceService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkExperienceResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(workExperienceService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkExperienceResponse>> create(@Valid @RequestBody WorkExperienceRequest request) {
        WorkExperienceResponse created = workExperienceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Work experience created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkExperienceResponse>> update(
            @PathVariable Long id, @Valid @RequestBody WorkExperienceRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Work experience updated", workExperienceService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workExperienceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
