package com.altees.portfolio.sociallink.controller;

import com.altees.portfolio.common.response.ApiResponse;
import com.altees.portfolio.sociallink.dto.SocialLinkRequest;
import com.altees.portfolio.sociallink.dto.SocialLinkResponse;
import com.altees.portfolio.sociallink.service.SocialLinkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profile/social-links")
public class SocialLinkController {

    private final SocialLinkService socialLinkService;

    public SocialLinkController(SocialLinkService socialLinkService) {
        this.socialLinkService = socialLinkService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SocialLinkResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(socialLinkService.findAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SocialLinkResponse>> create(@Valid @RequestBody SocialLinkRequest request) {
        SocialLinkResponse created = socialLinkService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Social link added", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SocialLinkResponse>> update(
            @PathVariable Long id, @Valid @RequestBody SocialLinkRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Social link updated", socialLinkService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        socialLinkService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
