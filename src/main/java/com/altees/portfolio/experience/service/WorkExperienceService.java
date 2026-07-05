package com.altees.portfolio.experience.service;

import com.altees.portfolio.common.exception.ResourceNotFoundException;
import com.altees.portfolio.experience.dto.WorkExperienceRequest;
import com.altees.portfolio.experience.dto.WorkExperienceResponse;
import com.altees.portfolio.experience.entity.WorkExperience;
import com.altees.portfolio.experience.repository.WorkExperienceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class WorkExperienceService {

    private final WorkExperienceRepository workExperienceRepository;

    public WorkExperienceService(WorkExperienceRepository workExperienceRepository) {
        this.workExperienceRepository = workExperienceRepository;
    }

    @Transactional(readOnly = true)
    public List<WorkExperienceResponse> findAll() {
        return workExperienceRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WorkExperienceResponse findById(Long id) {
        return workExperienceRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("WorkExperience", id));
    }

    public WorkExperienceResponse create(WorkExperienceRequest request) {
        WorkExperience experience = new WorkExperience();
        applyRequest(experience, request);
        return toResponse(workExperienceRepository.save(experience));
    }

    public WorkExperienceResponse update(Long id, WorkExperienceRequest request) {
        WorkExperience experience = workExperienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkExperience", id));
        applyRequest(experience, request);
        return toResponse(workExperienceRepository.save(experience));
    }

    public void delete(Long id) {
        if (!workExperienceRepository.existsById(id)) {
            throw new ResourceNotFoundException("WorkExperience", id);
        }
        workExperienceRepository.deleteById(id);
    }

    private void applyRequest(WorkExperience experience, WorkExperienceRequest request) {
        experience.setCompany(request.company());
        experience.setRole(request.role());
        experience.setLocation(request.location());
        experience.setStartDate(request.startDate());
        experience.setEndDate(request.endDate());
        experience.setCurrent(request.current());
        experience.setDescription(request.description());
        experience.setKeyAchievements(request.keyAchievements() != null ? new ArrayList<>(request.keyAchievements()) : new ArrayList<>());
    }

    public WorkExperienceResponse toResponse(WorkExperience experience) {
        return new WorkExperienceResponse(
                experience.getId(),
                experience.getCompany(),
                experience.getRole(),
                experience.getLocation(),
                experience.getStartDate(),
                experience.getEndDate(),
                experience.isCurrent(),
                experience.getDescription(),
                experience.getKeyAchievements(),
                experience.getCreatedAt(),
                experience.getUpdatedAt()
        );
    }
}
