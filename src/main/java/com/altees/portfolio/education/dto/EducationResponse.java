package com.altees.portfolio.education.dto;

import java.time.LocalDateTime;

public record EducationResponse(
        Long id,
        String institution,
        String degree,
        String fieldOfStudy,
        Integer startYear,
        Integer endYear,
        String grade,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
