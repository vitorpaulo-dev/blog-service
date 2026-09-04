package dev.vitorpaulo.blog.output.tag;

import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.output.mapper.TagMapper;
import dev.vitorpaulo.blog.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagOutputTest {

    @Mock private TagRepository tagRepository;
    @Mock private TagMapper tagMapper;

    @InjectMocks
    private TagOutput tagOutput;

    @Test
    void shouldReturnEmptyListWhenIdsIsNull() {
        var result = tagOutput.findAllById(null);
        assertTrue(result.isEmpty());
        verifyNoInteractions(tagRepository);
    }

    @Test
    void shouldReturnEmptyListWhenIdsIsEmpty() {
        var result = tagOutput.findAllById(List.of());
        assertTrue(result.isEmpty());
        verifyNoInteractions(tagRepository);
    }

    @Test
    void shouldReturnTagsWhenFound() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var entity1 = mock(dev.vitorpaulo.blog.domain.TagEntity.class);
        var entity2 = mock(dev.vitorpaulo.blog.domain.TagEntity.class);
        var model1 = new TagModel(id1, "slug-1", null);
        var model2 = new TagModel(id2, "slug-2", null);

        when(tagRepository.findAllById(List.of(id1, id2))).thenReturn(List.of(entity1, entity2));
        when(tagMapper.toModel(entity1)).thenReturn(model1);
        when(tagMapper.toModel(entity2)).thenReturn(model2);

        var result = tagOutput.findAllById(List.of(id1, id2));

        assertEquals(2, result.size());
        assertEquals(model1, result.get(0));
        assertEquals(model2, result.get(1));
    }
}
