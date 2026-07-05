package com.altees.portfolio.education.controller;

import com.altees.portfolio.common.response.ApiResponse;
import com.altees.portfolio.education.dto.EducationRequest;
import com.altees.portfolio.education.dto.EducationResponse;
import com.altees.portfolio.education.service.EducationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/education")
public class EducationController {

    private final EducationService educationService;

    public EducationController(EducationService educationService) {
        this.educationService = educationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EducationResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(educationService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EducationResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(educationService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EducationResponse>> create(@Valid @RequestBody EducationRequest request) {
        EducationResponse created = educationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Education entry created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EducationResponse>> update(
            @PathVariable Long id, @Valid @RequestBody EducationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Education entry updated", educationService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        educationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
