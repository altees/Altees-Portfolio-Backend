package com.altees.portfolio.skill.repository;

import com.altees.portfolio.skill.entity.Skill;
import com.altees.portfolio.skill.entity.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findAllByCategory(SkillCategory category);
}
