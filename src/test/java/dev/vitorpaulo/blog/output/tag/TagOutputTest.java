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
    @Mock private TagEntity tagEntity1;
    @Mock private TagEntity tagEntity2;
    @Mock private TagModel tagModel1;
    @Mock private TagModel tagModel2;

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

        when(tagRepository.findAllById(List.of(id1, id2))).thenReturn(List.of(tagEntity1, tagEntity2));
        when(tagMapper.toModel(tagEntity1)).thenReturn(tagModel1);
        when(tagMapper.toModel(tagEntity2)).thenReturn(tagModel2);

        var result = tagOutput.findAllById(List.of(id1, id2));

        assertEquals(2, result.size());
        assertEquals(tagModel1, result.get(0));
        assertEquals(tagModel2, result.get(1));
    }
}
