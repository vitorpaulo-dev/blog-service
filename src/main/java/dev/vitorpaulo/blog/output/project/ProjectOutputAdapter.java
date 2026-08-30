package dev.vitorpaulo.blog.output.project;

import dev.vitorpaulo.blog.mapper.PostMapper;
import dev.vitorpaulo.blog.model.post.Project;
import dev.vitorpaulo.blog.output.ProjectOutput;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProjectOutputAdapter implements ProjectOutput {

    private final ProjectRepository projectRepository;
    private final PostMapper postMapper;

    @Override
    public List<Project> findAllById(List<UUID> ids) {
        return projectRepository.findAllById(ids).stream().map(postMapper::map).toList();
    }
}
