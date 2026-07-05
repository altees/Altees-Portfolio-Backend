package com.altees.portfolio.education.service;

import com.altees.portfolio.common.exception.ResourceNotFoundException;
import com.altees.portfolio.education.dto.EducationRequest;
import com.altees.portfolio.education.dto.EducationResponse;
import com.altees.portfolio.education.entity.Education;
import com.altees.portfolio.education.repository.EducationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EducationService {

    private final EducationRepository educationRepository;

    public EducationService(EducationRepository educationRepository) {
        this.educationRepository = educationRepository;
    }

    @Transactional(readOnly = true)
    public List<EducationResponse> findAll() {
        return educationRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EducationResponse findById(Long id) {
        return educationRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Education", id));
    }

    public EducationResponse create(EducationRequest request) {
        Education education = new Education();
        applyRequest(education, request);
        return toResponse(educationRepository.save(education));
    }

    public EducationResponse update(Long id, EducationRequest request) {
        Education education = educationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Education", id));
        applyRequest(education, request);
        return toResponse(educationRepository.save(education));
    }

    public void delete(Long id) {
        if (!educationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Education", id);
        }
        educationRepository.deleteById(id);
    }

    private void applyRequest(Education education, EducationRequest request) {
        education.setInstitution(request.institution());
        education.setDegree(request.degree());
        education.setFieldOfStudy(request.fieldOfStudy());
        education.setStartYear(request.startYear());
        education.setEndYear(request.endYear());
        education.setGrade(request.grade());
        education.setDescription(request.description());
    }

    public EducationResponse toResponse(Education education) {
        return new EducationResponse(
                education.getId(),
                education.getInstitution(),
                education.getDegree(),
                education.getFieldOfStudy(),
                education.getStartYear(),
                education.getEndYear(),
                education.getGrade(),
                education.getDescription(),
                education.getCreatedAt(),
                education.getUpdatedAt()
        );
    }
}
