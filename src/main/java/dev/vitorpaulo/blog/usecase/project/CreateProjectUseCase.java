package dev.vitorpaulo.blog.usecase.project;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateProjectUseCase {

    private final ProjectOutput projectOutput;

    public ProjectModel execute(ProjectModel project, AuthorModel author) {
        return projectOutput.save(project, author);
    }
}
