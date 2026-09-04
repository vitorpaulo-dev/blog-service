package dev.vitorpaulo.blog.output.tag;

import dev.vitorpaulo.blog.domain.TagEntity;
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
    @Mock private TagEntity tagEntity;
    @Mock private TagModel tagModel;

    @InjectMocks
    private TagOutput tagOutput;

    @Test
    void findAllById_nullIds_returnsEmptyList() {
        var result = tagOutput.findAllById(null);
        assertTrue(result.isEmpty());
        verifyNoInteractions(tagRepository);
    }

    @Test
    void findAllById_emptyIds_returnsEmptyList() {
        var result = tagOutput.findAllById(List.of());
        assertTrue(result.isEmpty());
        verifyNoInteractions(tagRepository);
    }

    @Test
    void findAllById_withIds_returnsMappedModels() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var entity1 = mock(TagEntity.class);
        var entity2 = mock(TagEntity.class);
        var model1 = mock(TagModel.class);
        var model2 = mock(TagModel.class);

        when(tagRepository.findAllById(List.of(id1, id2))).thenReturn(List.of(entity1, entity2));
        when(tagMapper.toModel(entity1)).thenReturn(model1);
        when(tagMapper.toModel(entity2)).thenReturn(model2);

        var result = tagOutput.findAllById(List.of(id1, id2));

        assertEquals(2, result.size());
        assertEquals(model1, result.get(0));
        assertEquals(model2, result.get(1));
    }
}
