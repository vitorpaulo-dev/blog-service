package dev.vitorpaulo.blog.usecase.project;

import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetProjectBySlugUseCase {

    private final ProjectOutput projectOutput;

    public ProjectModel execute(String slug, Language language) {
        return projectOutput.findBySlugAndIncrementView(slug, language);
    }
}
