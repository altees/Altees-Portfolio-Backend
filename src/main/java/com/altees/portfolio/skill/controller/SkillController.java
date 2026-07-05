package com.altees.portfolio.skill.controller;

import com.altees.portfolio.common.response.ApiResponse;
import com.altees.portfolio.skill.dto.SkillRequest;
import com.altees.portfolio.skill.dto.SkillResponse;
import com.altees.portfolio.skill.entity.SkillCategory;
import com.altees.portfolio.skill.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/skills")
public class SkillController {

    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SkillResponse>>> getAll(
            @RequestParam(required = false) SkillCategory category) {
        return ResponseEntity.ok(ApiResponse.ok(skillService.findAll(category)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SkillResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(skillService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SkillResponse>> create(@Valid @RequestBody SkillRequest request) {
        SkillResponse created = skillService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Skill created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SkillResponse>> update(
            @PathVariable Long id, @Valid @RequestBody SkillRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Skill updated", skillService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        skillService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
