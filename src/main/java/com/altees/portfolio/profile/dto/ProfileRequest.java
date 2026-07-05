package com.altees.portfolio.profile.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProfileRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Email is required") @Email(message = "Valid email is required") String email,
        String phone,
        String tagline,
        String summary,
        String location,
        String avatarUrl,
        String resumeUrl
) {}
