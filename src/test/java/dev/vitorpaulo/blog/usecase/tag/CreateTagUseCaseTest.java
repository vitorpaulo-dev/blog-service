package dev.vitorpaulo.blog.usecase.tag;

import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.output.tag.TagOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTagUseCaseTest {

    @Mock private TagOutput tagOutput;
    @Mock private TagModel tag;
    @Mock private TagModel savedTag;

    @InjectMocks
    private CreateTagUseCase createTagUseCase;

    @Test
    void execute_returnsSavedTag() {
        when(tagOutput.save(tag)).thenReturn(savedTag);

        var result = createTagUseCase.execute(tag);

        assertEquals(savedTag, result);
        verify(tagOutput).save(tag);
    }
}
