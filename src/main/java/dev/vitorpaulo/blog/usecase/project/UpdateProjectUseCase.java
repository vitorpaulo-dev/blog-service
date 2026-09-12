package dev.vitorpaulo.blog.usecase.project;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateProjectUseCase {

    private final ProjectOutput projectOutput;

    public ProjectModel execute(ProjectModel project, List<UUID> tagIds, AuthorModel author) {
        return projectOutput.update(project, tagIds, author);
    }
}
