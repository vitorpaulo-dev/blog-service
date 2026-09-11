package dev.vitorpaulo.blog.usecase.project;

import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetProjectByIdUseCase {

    private final ProjectOutput projectOutput;

    public ProjectModel execute(UUID id) {
        return projectOutput.findById(id);
    }
}
