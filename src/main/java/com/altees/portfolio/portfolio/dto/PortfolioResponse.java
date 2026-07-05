package com.altees.portfolio.portfolio.dto;

import com.altees.portfolio.certification.dto.CertificationResponse;
import com.altees.portfolio.education.dto.EducationResponse;
import com.altees.portfolio.experience.dto.WorkExperienceResponse;
import com.altees.portfolio.profile.dto.ProfileResponse;
import com.altees.portfolio.project.dto.ProjectResponse;
import com.altees.portfolio.skill.dto.SkillResponse;

import java.util.List;

public record PortfolioResponse(
        ProfileResponse profile,
        List<SkillResponse> skills,
        List<EducationResponse> education,
        List<WorkExperienceResponse> experiences,
        List<CertificationResponse> certifications,
        List<ProjectResponse> projects
) {}
