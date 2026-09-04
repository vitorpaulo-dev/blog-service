package dev.vitorpaulo.blog.usecase.post;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.model.PostQueryModel;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.output.post.PostOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchPostUseCaseTest {

    @Mock private PostOutput postOutput;
    @Mock private AuthorModel author;
    @Mock private PostQueryModel query;
    @Mock private PaginatedOutput<PostModel> expected;

    @InjectMocks
    private SearchPostUseCase searchPostUseCase;

    @Test
    void execute_withQuery_returnsPaginatedResults() {
        var input = new PaginatedInput<>(query, 0, 10, "createdAt", Sort.Direction.DESC);

        when(postOutput.search(input, author)).thenReturn(expected);

        var result = searchPostUseCase.execute(input, author);

        assertEquals(expected, result);
        verify(postOutput).search(input, author);
    }
}
