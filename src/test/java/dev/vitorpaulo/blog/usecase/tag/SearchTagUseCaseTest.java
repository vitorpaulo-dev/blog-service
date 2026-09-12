package dev.vitorpaulo.blog.usecase.tag;

import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.model.TagQueryModel;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import dev.vitorpaulo.blog.output.tag.TagOutput;
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
class SearchTagUseCaseTest {

    @Mock private TagOutput tagOutput;
    @Mock private PaginatedOutput<TagModel> paginatedOutput;

    @InjectMocks
    private SearchTagUseCase searchTagUseCase;

    @Test
    void execute_returnsPaginatedResult() {
        var input = new PaginatedInput<>(new TagQueryModel("java", Language.ENGLISH), 0, 10, "slug", Sort.Direction.ASC);

        when(tagOutput.search(input, Language.ENGLISH)).thenReturn(paginatedOutput);

        var result = searchTagUseCase.execute(input, Language.ENGLISH);

        assertEquals(paginatedOutput, result);
        verify(tagOutput).search(input, Language.ENGLISH);
    }
}
