package dev.vitorpaulo.blog.usecase.tag;

import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.output.tag.TagOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetTagByIdUseCaseTest {

    @Mock private TagOutput tagOutput;
    @Mock private TagModel tagModel;

    @InjectMocks
    private GetTagByIdUseCase getTagByIdUseCase;

    @Test
    void execute_returnsTag() {
        var id = UUID.randomUUID();
        when(tagOutput.findById(id)).thenReturn(tagModel);

        var result = getTagByIdUseCase.execute(id);

        assertEquals(tagModel, result);
        verify(tagOutput).findById(id);
    }
}
