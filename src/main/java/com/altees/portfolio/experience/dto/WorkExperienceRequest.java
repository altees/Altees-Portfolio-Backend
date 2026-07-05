package com.altees.portfolio.experience.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record WorkExperienceRequest(
        @NotBlank(message = "Company is required") String company,
        @NotBlank(message = "Role is required") String role,
        String location,
        @NotNull(message = "Start date is required") LocalDate startDate,
        LocalDate endDate,
        boolean current,
        String description,
        List<String> keyAchievements
) {
    @AssertTrue(message = "End date must be after start date")
    private boolean isEndDateValid() {
        return endDate == null || !endDate.isBefore(startDate);
    }

    @AssertTrue(message = "Current position must not have an end date")
    private boolean isCurrentConsistent() {
        return !current || endDate == null;
    }
}
