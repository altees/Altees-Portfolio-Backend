package com.altees.portfolio.sociallink.dto;

import com.altees.portfolio.sociallink.entity.SocialPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SocialLinkRequest(
        @NotNull(message = "Platform is required") SocialPlatform platform,
        @NotBlank(message = "URL is required") String url
) {}
