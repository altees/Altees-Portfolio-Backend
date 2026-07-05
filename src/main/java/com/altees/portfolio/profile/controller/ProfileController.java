package com.altees.portfolio.profile.controller;

import com.altees.portfolio.common.response.ApiResponse;
import com.altees.portfolio.profile.dto.ProfileRequest;
import com.altees.portfolio.profile.dto.ProfileResponse;
import com.altees.portfolio.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile() {
        return ResponseEntity.ok(ApiResponse.ok(profileService.findProfile()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> createProfile(@Valid @RequestBody ProfileRequest request) {
        ProfileResponse created = profileService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Profile created", created));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(@Valid @RequestBody ProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", profileService.updateProfile(request)));
    }
}
