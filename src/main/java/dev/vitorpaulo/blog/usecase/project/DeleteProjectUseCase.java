package dev.vitorpaulo.blog.usecase.project;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeleteProjectUseCase {

    private final ProjectOutput projectOutput;

    public void execute(List<UUID> ids, AuthorModel author) {
        projectOutput.deleteAll(ids, author);
    }
}
