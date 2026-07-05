package com.altees.portfolio.skill.dto;

import com.altees.portfolio.skill.entity.ProficiencyLevel;
import com.altees.portfolio.skill.entity.SkillCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SkillRequest(
        @NotBlank(message = "Skill name is required") String name,
        @NotNull(message = "Category is required") SkillCategory category,
        ProficiencyLevel proficiencyLevel,
        @Min(value = 0, message = "Years of experience must be zero or more") Integer yearsOfExperience
) {}
