package com.altees.portfolio.project.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ProjectRequest(
        @NotBlank(message = "Project title is required") String title,
        @NotBlank(message = "Project description is required") String description,
        List<String> techStack,
        String liveUrl,
        String repoUrl,
        String thumbnailUrl,
        boolean featured
) {}
