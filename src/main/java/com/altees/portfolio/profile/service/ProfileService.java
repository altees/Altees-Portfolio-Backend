package com.altees.portfolio.profile.service;

import com.altees.portfolio.common.exception.DuplicateResourceException;
import com.altees.portfolio.common.exception.ResourceNotFoundException;
import com.altees.portfolio.profile.dto.ProfileRequest;
import com.altees.portfolio.profile.dto.ProfileResponse;
import com.altees.portfolio.profile.entity.Profile;
import com.altees.portfolio.profile.repository.ProfileRepository;
import com.altees.portfolio.sociallink.dto.SocialLinkResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse findProfile() {
        return profileRepository.findAll()
                .stream()
                .findFirst()
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    public ProfileResponse createProfile(ProfileRequest request) {
        if (profileRepository.count() > 0) {
            throw new DuplicateResourceException("Profile already exists. Use PUT to update it.");
        }
        Profile profile = new Profile();
        applyRequest(profile, request);
        return toResponse(profileRepository.save(profile));
    }

    public ProfileResponse updateProfile(ProfileRequest request) {
        Profile profile = profileRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        applyRequest(profile, request);
        return toResponse(profileRepository.save(profile));
    }

    private void applyRequest(Profile profile, ProfileRequest request) {
        profile.setName(request.name());
        profile.setEmail(request.email());
        profile.setPhone(request.phone());
        profile.setTagline(request.tagline());
        profile.setSummary(request.summary());
        profile.setLocation(request.location());
        profile.setAvatarUrl(request.avatarUrl());
        profile.setResumeUrl(request.resumeUrl());
    }

    public ProfileResponse toResponse(Profile profile) {
        List<SocialLinkResponse> socialLinks = profile.getSocialLinks().stream()
                .map(sl -> new SocialLinkResponse(sl.getId(), sl.getPlatform(), sl.getUrl(), sl.getCreatedAt()))
                .toList();
        return new ProfileResponse(
                profile.getId(),
                profile.getName(),
                profile.getEmail(),
                profile.getPhone(),
                profile.getTagline(),
                profile.getSummary(),
                profile.getLocation(),
                profile.getAvatarUrl(),
                profile.getResumeUrl(),
                socialLinks,
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }

    public List<Profile> findAll() {
        return profileRepository.findAll();
    }
}
