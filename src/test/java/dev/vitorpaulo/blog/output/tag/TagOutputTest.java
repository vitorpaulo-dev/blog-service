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

import java.util.HashMap;
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
    @Mock private TagEntity tagEntity;
    @Mock private TagEntity secondTagEntity;
    @Mock private TagModel expectedResult;
    @Mock private TagModel secondResult;

    @InjectMocks
    private TagOutput tagOutput;

    @Test
    void findById_found_usesFindByIdWithContents() {
        var id = UUID.randomUUID();
        when(tagRepository.findByIdWithContents(id)).thenReturn(Optional.of(tagEntity));
        when(tagOutputMapper.toModel(tagEntity)).thenReturn(expectedResult);

        var result = tagOutput.findById(id);

        assertEquals(expectedResult, result);
        verify(tagRepository, never()).findById(any());
    }

    @Test
    void findById_notFound_throwsNotFoundException() {
        when(tagRepository.findByIdWithContents(any())).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class, () -> tagOutput.findById(UUID.randomUUID()));
        assertEquals(ExceptionCode.TAG_NOT_FOUND, ex.getCode());
    }

    @Test
    void findAllById_withIds_usesFindWithSingleContent() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        when(tagRepository.findWithSingleContent(List.of(id1, id2), Language.PORTUGUESE))
                .thenReturn(List.of(tagEntity, secondTagEntity));
        when(tagOutputMapper.toModel(tagEntity)).thenReturn(expectedResult);
        when(tagOutputMapper.toModel(secondTagEntity)).thenReturn(secondResult);
        when(expectedResult.translations()).thenReturn(java.util.Map.of());
        when(secondResult.translations()).thenReturn(java.util.Map.of());

        var result = tagOutput.findAllById(List.of(id1, id2), Language.PORTUGUESE);

        assertEquals(List.of(expectedResult, secondResult), result);
        verify(tagRepository, never()).findAllById(anyList());
    }

    @Test
    void findAllById_nullIds_returnsEmptyList() {
        var result = tagOutput.findAllById(null, Language.ENGLISH);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllById_emptyIds_returnsEmptyList() {
        var result = tagOutput.findAllById(List.of(), Language.ENGLISH);

        assertTrue(result.isEmpty());
    }

    // Committed behavior: batch filters non-requested languages in Java (removeIf).
    @Test
    void findAllById_filtersOtherLanguagesInJava() {
        var translations = new HashMap<Language, dev.vitorpaulo.blog.model.TagContentModel>();
        translations.put(Language.ENGLISH, null);
        translations.put(Language.PORTUGUESE, null);
        when(tagRepository.findWithSingleContent(anyList(), eq(Language.PORTUGUESE))).thenReturn(List.of(tagEntity));
        when(tagOutputMapper.toModel(tagEntity)).thenReturn(expectedResult);
        when(expectedResult.translations()).thenReturn(translations);

        var result = tagOutput.findAllById(List.of(UUID.randomUUID()), Language.PORTUGUESE);

        assertEquals(1, result.size());
        assertTrue(translations.containsKey(Language.PORTUGUESE));
        assertFalse(translations.containsKey(Language.ENGLISH));
    }
}
