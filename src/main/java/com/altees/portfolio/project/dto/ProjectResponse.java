package com.altees.portfolio.project.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectResponse(
        Long id,
        String title,
        String description,
        List<String> techStack,
        String liveUrl,
        String repoUrl,
        String thumbnailUrl,
        boolean featured,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
