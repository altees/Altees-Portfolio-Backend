package com.altees.portfolio.sociallink.dto;

import com.altees.portfolio.sociallink.entity.SocialPlatform;

import java.time.LocalDateTime;

public record SocialLinkResponse(
        Long id,
        SocialPlatform platform,
        String url,
        LocalDateTime createdAt
) {}
