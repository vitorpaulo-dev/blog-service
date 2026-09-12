package dev.vitorpaulo.blog.usecase.project;

import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetProjectByBatchUseCase {

    private final ProjectOutput projectOutput;

    public List<ProjectModel> execute(List<UUID> ids, Language language) {
        return projectOutput.findAllById(ids, language);
    }
}
