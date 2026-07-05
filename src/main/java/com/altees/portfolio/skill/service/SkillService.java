package com.altees.portfolio.skill.service;

import com.altees.portfolio.common.exception.ResourceNotFoundException;
import com.altees.portfolio.skill.dto.SkillRequest;
import com.altees.portfolio.skill.dto.SkillResponse;
import com.altees.portfolio.skill.entity.Skill;
import com.altees.portfolio.skill.entity.SkillCategory;
import com.altees.portfolio.skill.repository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    @Transactional(readOnly = true)
    public List<SkillResponse> findAll(SkillCategory category) {
        List<Skill> skills = (category != null)
                ? skillRepository.findAllByCategory(category)
                : skillRepository.findAll();
        return skills.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public SkillResponse findById(Long id) {
        return skillRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", id));
    }

    public SkillResponse create(SkillRequest request) {
        Skill skill = new Skill();
        applyRequest(skill, request);
        return toResponse(skillRepository.save(skill));
    }

    public SkillResponse update(Long id, SkillRequest request) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", id));
        applyRequest(skill, request);
        return toResponse(skillRepository.save(skill));
    }

    public void delete(Long id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill", id));
        skillRepository.delete(skill);
    }

    private void applyRequest(Skill skill, SkillRequest request) {
        skill.setName(request.name());
        skill.setCategory(request.category());
        skill.setProficiencyLevel(request.proficiencyLevel());
        skill.setYearsOfExperience(request.yearsOfExperience());
    }

    public SkillResponse toResponse(Skill skill) {
        return new SkillResponse(
                skill.getId(),
                skill.getName(),
                skill.getCategory(),
                skill.getProficiencyLevel(),
                skill.getYearsOfExperience(),
                skill.getCreatedAt(),
                skill.getUpdatedAt()
        );
    }
}
