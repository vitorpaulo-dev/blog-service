package dev.vitorpaulo.blog.usecase.project;

import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.model.ProjectStatus;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import dev.vitorpaulo.blog.output.view.ViewCounterOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetProjectBySlugUseCase {

    private final ProjectOutput projectOutput;
    private final ViewCounterOutput viewCounterOutput;

    public ProjectModel execute(String slug, Language language, String ip) {
        final var project = projectOutput.findBySlug(slug, language);
        if (project.status() == ProjectStatus.PUBLISHED) {
            viewCounterOutput.countProjectView(project.id(), ip);
        }
        return project;
    }
}
