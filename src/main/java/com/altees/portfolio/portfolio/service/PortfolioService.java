package com.altees.portfolio.portfolio.service;

import com.altees.portfolio.certification.service.CertificationService;
import com.altees.portfolio.education.service.EducationService;
import com.altees.portfolio.experience.service.WorkExperienceService;
import com.altees.portfolio.portfolio.dto.PortfolioResponse;
import com.altees.portfolio.profile.service.ProfileService;
import com.altees.portfolio.project.service.ProjectService;
import com.altees.portfolio.skill.service.SkillService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
@Transactional(readOnly = true)
public class PortfolioService {

    private final ProfileService profileService;
    private final SkillService skillService;
    private final EducationService educationService;
    private final WorkExperienceService workExperienceService;
    private final CertificationService certificationService;
    private final ProjectService projectService;

    public PortfolioService(
            ProfileService profileService,
            SkillService skillService,
            EducationService educationService,
            WorkExperienceService workExperienceService,
            CertificationService certificationService,
            ProjectService projectService) {
        this.profileService = profileService;
        this.skillService = skillService;
        this.educationService = educationService;
        this.workExperienceService = workExperienceService;
        this.certificationService = certificationService;
        this.projectService = projectService;
    }

    public PortfolioResponse getPortfolio() {
 
        return new PortfolioResponse(
                profileService.findProfile(),
                skillService.findAll(null),
                educationService.findAll(),
                workExperienceService.findAll(),
                certificationService.findAll(),
                projectService.findAll(null)
        );
    }
}
