package com.altees.portfolio.education.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EducationRequest(
        @NotBlank(message = "Institution is required") String institution,
        @NotBlank(message = "Degree is required") String degree,
        @NotBlank(message = "Field of study is required") String fieldOfStudy,
        @NotNull(message = "Start year is required") @Min(1900) @Max(2100) Integer startYear,
        @Min(1900) @Max(2100) Integer endYear,
        String grade,
        String description
) {}
