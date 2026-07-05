package com.altees.portfolio.education.repository;

import com.altees.portfolio.education.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EducationRepository extends JpaRepository<Education, Long> {
}
