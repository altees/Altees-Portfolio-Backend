package com.altees.portfolio.sociallink.service;

import com.altees.portfolio.common.exception.ResourceNotFoundException;
import com.altees.portfolio.profile.entity.Profile;
import com.altees.portfolio.profile.service.ProfileService;
import com.altees.portfolio.sociallink.dto.SocialLinkRequest;
import com.altees.portfolio.sociallink.dto.SocialLinkResponse;
import com.altees.portfolio.sociallink.entity.SocialLink;
import com.altees.portfolio.sociallink.repository.SocialLinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SocialLinkService {

    private final SocialLinkRepository socialLinkRepository;
    private final ProfileService profileService;

    public SocialLinkService(SocialLinkRepository socialLinkRepository, ProfileService profileService) {
        this.socialLinkRepository = socialLinkRepository;
        this.profileService = profileService;
    }

    @Transactional(readOnly = true)
    public List<SocialLinkResponse> findAll() {
        return socialLinkRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public SocialLinkResponse create(SocialLinkRequest request) {
        Profile profile = resolveProfile();
        SocialLink socialLink = new SocialLink();
        socialLink.setPlatform(request.platform());
        socialLink.setUrl(request.url());
        socialLink.setProfile(profile);
        return toResponse(socialLinkRepository.save(socialLink));
    }

    public SocialLinkResponse update(Long id, SocialLinkRequest request) {
        SocialLink socialLink = socialLinkRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SocialLink", id));
        socialLink.setPlatform(request.platform());
        socialLink.setUrl(request.url());
        return toResponse(socialLinkRepository.save(socialLink));
    }

    public void delete(Long id) {
        if (!socialLinkRepository.existsById(id)) {
            throw new ResourceNotFoundException("SocialLink", id);
        }
        socialLinkRepository.deleteById(id);
    }

    private Profile resolveProfile() {
        return profileService.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found. Create a profile first."));
    }

    public SocialLinkResponse toResponse(SocialLink socialLink) {
        return new SocialLinkResponse(
                socialLink.getId(),
                socialLink.getPlatform(),
                socialLink.getUrl(),
                socialLink.getCreatedAt()
        );
    }
}
