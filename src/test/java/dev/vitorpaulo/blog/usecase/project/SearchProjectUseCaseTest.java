package dev.vitorpaulo.blog.usecase.project;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.ProjectModel;
import dev.vitorpaulo.blog.model.ProjectQueryModel;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.output.project.ProjectOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchProjectUseCaseTest {

    @Mock private ProjectOutput projectOutput;
    @Mock private AuthorModel author;
    @Mock private ProjectQueryModel query;
    @Mock private PaginatedOutput<ProjectModel> expected;

    @InjectMocks
    private SearchProjectUseCase searchProjectUseCase;

    @Test
    void execute_withQuery_returnsPaginatedResults() {
        var input = new PaginatedInput<>(query, 0, 10, "createdAt", Sort.Direction.DESC);

        when(projectOutput.search(input, author)).thenReturn(expected);

        var result = searchProjectUseCase.execute(input, author);

        assertEquals(expected, result);
        verify(projectOutput).search(input, author);
    }
}
