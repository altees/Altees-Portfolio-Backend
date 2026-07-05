package com.altees.portfolio.project.service;

import com.altees.portfolio.common.exception.ResourceNotFoundException;
import com.altees.portfolio.project.dto.ProjectRequest;
import com.altees.portfolio.project.dto.ProjectResponse;
import com.altees.portfolio.project.entity.Project;
import com.altees.portfolio.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> findAll(Boolean featured) {
        List<Project> projects = (featured != null)
                ? projectRepository.findAllByFeatured(featured)
                : projectRepository.findAll();
        return projects.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(Long id) {
        return projectRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    public ProjectResponse create(ProjectRequest request) {
        Project project = new Project();
        applyRequest(project, request);
        return toResponse(projectRepository.save(project));
    }

    public ProjectResponse update(Long id, ProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
        applyRequest(project, request);
        return toResponse(projectRepository.save(project));
    }

    public void delete(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project", id);
        }
        projectRepository.deleteById(id);
    }

    private void applyRequest(Project project, ProjectRequest request) {
        project.setTitle(request.title());
        project.setDescription(request.description());
        project.setTechStack(request.techStack() != null ? new ArrayList<>(request.techStack()) : new ArrayList<>());
        project.setLiveUrl(request.liveUrl());
        project.setRepoUrl(request.repoUrl());
        project.setThumbnailUrl(request.thumbnailUrl());
        project.setFeatured(request.featured());
    }

    public ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getTechStack(),
                project.getLiveUrl(),
                project.getRepoUrl(),
                project.getThumbnailUrl(),
                project.isFeatured(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
