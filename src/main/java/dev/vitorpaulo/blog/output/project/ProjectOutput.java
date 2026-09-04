package dev.vitorpaulo.blog.output.project;

import dev.vitorpaulo.blog.output.mapper.ProjectMapper;
import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProjectOutput {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public List<ProjectModel> findAllById(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return projectRepository.findAllById(ids).stream().map(projectMapper::toModel).toList();
    }
}
