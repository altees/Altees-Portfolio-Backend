package com.altees.portfolio.certification.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CertificationResponse(
        Long id,
        String name,
        String issuingOrganization,
        LocalDate issueDate,
        LocalDate expiryDate,
        String credentialId,
        String credentialUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
