package com.altees.portfolio.profile.repository;

import com.altees.portfolio.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
