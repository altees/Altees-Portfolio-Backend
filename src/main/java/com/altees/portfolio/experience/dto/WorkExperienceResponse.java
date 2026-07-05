package com.altees.portfolio.experience.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record WorkExperienceResponse(
        Long id,
        String company,
        String role,
        String location,
        LocalDate startDate,
        LocalDate endDate,
        boolean current,
        String description,
        List<String> keyAchievements,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
