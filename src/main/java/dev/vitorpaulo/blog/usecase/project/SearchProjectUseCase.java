package dev.vitorpaulo.blog.usecase.project;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.model.ProjectQueryModel;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchProjectUseCase {

    private final ProjectOutput projectOutput;

    public PaginatedOutput<ProjectModel> execute(PaginatedInput<ProjectQueryModel> pageableInput, AuthorModel author) {
        return projectOutput.search(pageableInput, author);
    }
}
