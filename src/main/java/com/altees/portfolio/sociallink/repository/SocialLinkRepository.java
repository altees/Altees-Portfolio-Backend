package com.altees.portfolio.sociallink.repository;

import com.altees.portfolio.sociallink.entity.SocialLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {
    List<SocialLink> findAllByProfileId(Long profileId);
}
