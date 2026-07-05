package com.altees.portfolio.certification.controller;

import com.altees.portfolio.certification.dto.CertificationRequest;
import com.altees.portfolio.certification.dto.CertificationResponse;
import com.altees.portfolio.certification.service.CertificationService;
import com.altees.portfolio.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/certifications")
public class CertificationController {

    private final CertificationService certificationService;

    public CertificationController(CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CertificationResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(certificationService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CertificationResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(certificationService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CertificationResponse>> create(@Valid @RequestBody CertificationRequest request) {
        CertificationResponse created = certificationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Certification created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CertificationResponse>> update(
            @PathVariable Long id, @Valid @RequestBody CertificationRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Certification updated", certificationService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        certificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
