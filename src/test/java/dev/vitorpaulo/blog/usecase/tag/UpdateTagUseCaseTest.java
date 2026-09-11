package dev.vitorpaulo.blog.usecase.tag;

import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.output.tag.TagOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateTagUseCaseTest {

    @Mock private TagOutput tagOutput;
    @Mock private TagModel tag;
    @Mock private TagModel updatedTag;

    @InjectMocks
    private UpdateTagUseCase updateTagUseCase;

    @Test
    void execute_returnsUpdatedTag() {
        var id = UUID.randomUUID();
        when(tag.slug()).thenReturn("slug");
        when(tag.translations()).thenReturn(Map.of());
        when(tagOutput.update(any(TagModel.class))).thenReturn(updatedTag);

        var result = updateTagUseCase.execute(id, tag);

        assertEquals(updatedTag, result);
        verify(tagOutput).update(argThat(m -> m.id().equals(id)));
    }
}
