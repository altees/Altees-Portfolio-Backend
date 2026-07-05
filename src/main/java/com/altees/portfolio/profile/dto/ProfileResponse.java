package com.altees.portfolio.profile.dto;

import com.altees.portfolio.sociallink.dto.SocialLinkResponse;

import java.time.LocalDateTime;
import java.util.List;

public record ProfileResponse(
        Long id,
        String name,
        String email,
        String phone,
        String tagline,
        String summary,
        String location,
        String avatarUrl,
        String resumeUrl,
        List<SocialLinkResponse> socialLinks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
