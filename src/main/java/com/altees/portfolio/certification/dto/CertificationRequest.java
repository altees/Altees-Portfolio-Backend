package com.altees.portfolio.certification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CertificationRequest(
        @NotBlank(message = "Certification name is required") String name,
        @NotBlank(message = "Issuing organization is required") String issuingOrganization,
        @NotNull(message = "Issue date is required") LocalDate issueDate,
        LocalDate expiryDate,
        String credentialId,
        String credentialUrl
) {}
