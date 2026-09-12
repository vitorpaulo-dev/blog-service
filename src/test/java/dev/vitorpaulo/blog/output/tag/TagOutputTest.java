package dev.vitorpaulo.blog.output.tag;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.domain.TagEntity;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.output.mapper.TagOutputMapper;
import dev.vitorpaulo.blog.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagOutputTest {

    @Mock private TagRepository tagRepository;
    @Mock private TagOutputMapper tagOutputMapper;
    @Mock private TagEntity tagEntity1;
    @Mock private TagEntity tagEntity2;
    @Mock private TagModel tagModel1;
    @Mock private TagModel tagModel2;

    @InjectMocks
    private TagOutput tagOutput;

    @Test
    void findById_found_usesFindByIdWithContentsAndReturnsTag() {
        var entityId = UUID.randomUUID();

        when(tagRepository.findByIdWithContents(entityId)).thenReturn(Optional.of(tagEntity1));
        when(tagOutputMapper.toModel(tagEntity1)).thenReturn(tagModel1);

        var result = tagOutput.findById(entityId);

        assertEquals(tagModel1, result);
        verify(tagRepository).findByIdWithContents(entityId);
        verify(tagRepository, never()).findById(any());
    }

    @Test
    void findById_notFound_throwsNotFoundException() {
        when(tagRepository.findByIdWithContents(any())).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class, () -> tagOutput.findById(UUID.randomUUID()));
        assertEquals(ExceptionCode.TAG_NOT_FOUND, ex.getCode());
    }

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
        when(tagOutputMapper.toModel(tagEntity1)).thenReturn(tagModel1);
        when(tagOutputMapper.toModel(tagEntity2)).thenReturn(tagModel2);

        var result = tagOutput.findAllById(List.of(id1, id2));

        assertEquals(2, result.size());
        assertEquals(tagModel1, result.get(0));
        assertEquals(tagModel2, result.get(1));
    }

    @Test
    void findAllByIdWithLanguage_usesSingleContentFetchAndOneArgMapper() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();

        when(tagRepository.findWithSingleContent(List.of(id1, id2), Language.PORTUGUESE))
            .thenReturn(List.of(tagEntity1, tagEntity2));
        when(tagOutputMapper.toModel(tagEntity1)).thenReturn(tagModel1);
        when(tagOutputMapper.toModel(tagEntity2)).thenReturn(tagModel2);

        var result = tagOutput.findAllById(List.of(id1, id2), Language.PORTUGUESE);

        assertEquals(2, result.size());
        assertEquals(tagModel1, result.get(0));
        assertEquals(tagModel2, result.get(1));
        verify(tagRepository).findWithSingleContent(List.of(id1, id2), Language.PORTUGUESE);
        verify(tagRepository, never()).findAllById(anyList());
    }

    @Test
    void findAllByIdWithLanguage_nullIds_returnsEmptyList() {
        var result = tagOutput.findAllById(null, Language.PORTUGUESE);
        assertTrue(result.isEmpty());
        verifyNoInteractions(tagRepository);
    }

    @Test
    void findAllByIdWithLanguage_emptyIds_returnsEmptyList() {
        var result = tagOutput.findAllById(List.of(), Language.PORTUGUESE);
        assertTrue(result.isEmpty());
        verifyNoInteractions(tagRepository);
    }
}
