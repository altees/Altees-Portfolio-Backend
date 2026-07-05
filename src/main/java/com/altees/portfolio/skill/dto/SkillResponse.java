package com.altees.portfolio.skill.dto;

import com.altees.portfolio.skill.entity.ProficiencyLevel;
import com.altees.portfolio.skill.entity.SkillCategory;

import java.time.LocalDateTime;

public record SkillResponse(
        Long id,
        String name,
        SkillCategory category,
        ProficiencyLevel proficiencyLevel,
        Integer yearsOfExperience,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
