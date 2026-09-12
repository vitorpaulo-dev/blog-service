package dev.vitorpaulo.blog.output.tag;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.domain.TagContentEntity;
import dev.vitorpaulo.blog.domain.TagEntity;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.TagContentModel;
import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.model.TagQueryModel;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.output.mapper.TagOutputMapper;
import dev.vitorpaulo.blog.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagOutputExtendedTest {

    @Mock private TagRepository tagRepository;
    @Mock private TagOutputMapper tagOutputMapper;
    @Mock private TagEntity tagEntity;
    @Mock private TagContentEntity existingContent;
    @Mock private TagContentModel tagContentModel;
    @Mock private TagModel tagModel;
    @Mock private TagModel expectedResult;

    @InjectMocks
    private TagOutput tagOutput;

    @Test
    void findById_found_returnsTag() {
        var id = UUID.randomUUID();
        when(tagRepository.findByIdWithContents(id)).thenReturn(Optional.of(tagEntity));
        when(tagOutputMapper.toModel(tagEntity)).thenReturn(expectedResult);

        var result = tagOutput.findById(id);

        assertEquals(expectedResult, result);
    }

    @Test
    void findById_notFound_throwsNotFoundException() {
        when(tagRepository.findByIdWithContents(any())).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class, () -> tagOutput.findById(UUID.randomUUID()));
        assertEquals(ExceptionCode.TAG_NOT_FOUND, ex.getCode());
    }

    @Test
    void findBySlug_found_mapsWithPlainMapper() {
        when(tagRepository.findBySlugAndLanguage("java", Language.ENGLISH)).thenReturn(Optional.of(tagEntity));
        when(tagOutputMapper.toModel(tagEntity)).thenReturn(expectedResult);

        var result = tagOutput.findBySlug("java", Language.ENGLISH);

        assertEquals(expectedResult, result);
    }

    @Test
    void findBySlug_notFound_throwsNotFoundException() {
        when(tagRepository.findBySlugAndLanguage(anyString(), any())).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class,
            () -> tagOutput.findBySlug("nonexistent", Language.ENGLISH));
        assertEquals(ExceptionCode.TAG_NOT_FOUND, ex.getCode());
    }

    @Test
    void save_withEnglishTranslation_savesTag() {
        var translations = Map.of(Language.ENGLISH, tagContentModel);
        when(tagModel.translations()).thenReturn(translations);
        when(tagContentModel.name()).thenReturn("Java");

        var contentEntity = new TagContentEntity();
        when(tagOutputMapper.toContentEntity(tagContentModel)).thenReturn(contentEntity);

        when(tagRepository.countBySlugAndIdNot(anyString(), isNull())).thenReturn(0L);
        when(tagOutputMapper.toModel(any(TagEntity.class))).thenReturn(expectedResult);
        when(tagRepository.save(any(TagEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        tagOutput.save(tagModel);

        verify(tagRepository).save(any(TagEntity.class));
    }

    @Test
    void save_withConflict_generatesUniqueSlug() {
        var translations = Map.of(Language.ENGLISH, tagContentModel);
        when(tagModel.translations()).thenReturn(translations);
        when(tagContentModel.name()).thenReturn("Java");

        var contentEntity = new TagContentEntity();
        when(tagOutputMapper.toContentEntity(tagContentModel)).thenReturn(contentEntity);

        when(tagRepository.countBySlugAndIdNot("java", null)).thenReturn(2L);
        when(tagOutputMapper.toModel(any(TagEntity.class))).thenReturn(expectedResult);
        when(tagRepository.save(any(TagEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        tagOutput.save(tagModel);

        verify(tagRepository).save(argThat(e -> e.getSlug().equals("java-3")));
    }

    @Test
    void save_emptyTranslations_usesEmptySlug() {
        when(tagModel.translations()).thenReturn(Map.of());

        when(tagRepository.countBySlugAndIdNot(anyString(), isNull())).thenReturn(0L);
        when(tagOutputMapper.toModel(any(TagEntity.class))).thenReturn(expectedResult);
        when(tagRepository.save(any(TagEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        tagOutput.save(tagModel);

        verify(tagRepository).save(any(TagEntity.class));
    }

    @Test
    void update_notFound_throwsNotFoundException() {
        var tagId = UUID.randomUUID();
        when(tagModel.id()).thenReturn(tagId);
        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class, () -> tagOutput.update(tagModel));
        assertEquals(ExceptionCode.TAG_NOT_FOUND, ex.getCode());
    }

    @Test
    void update_existingContent_updatesInPlace() {
        var tagId = UUID.randomUUID();
        var translations = Map.of(Language.ENGLISH, tagContentModel);
        when(tagModel.translations()).thenReturn(translations);
        when(tagModel.id()).thenReturn(tagId);
        when(tagContentModel.name()).thenReturn("New Name");

        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getName()).thenReturn("Old Name");

        var contents = new ArrayList<>(List.of(existingContent));
        when(tagEntity.getContents()).thenReturn(contents);

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tagEntity));
        when(tagRepository.save(tagEntity)).thenReturn(tagEntity);
        when(tagOutputMapper.toModel(tagEntity)).thenReturn(expectedResult);

        tagOutput.update(tagModel);

        verify(existingContent).setName("New Name");
        assertEquals(1, tagEntity.getContents().size());
    }

    @Test
    void update_nameChanged_regeneratesSlug() {
        var tagId = UUID.randomUUID();
        var translations = Map.of(Language.ENGLISH, tagContentModel);
        when(tagModel.translations()).thenReturn(translations);
        when(tagModel.id()).thenReturn(tagId);
        when(tagContentModel.name()).thenReturn("Changed Name");

        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getName()).thenReturn("Original Name");

        var contents = new ArrayList<>(List.of(existingContent));
        when(tagEntity.getId()).thenReturn(tagId);
        when(tagEntity.getContents()).thenReturn(contents);

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tagEntity));
        when(tagRepository.countBySlugAndIdNot("changed-name", tagId)).thenReturn(0L);
        when(tagRepository.save(tagEntity)).thenReturn(tagEntity);
        when(tagOutputMapper.toModel(tagEntity)).thenReturn(expectedResult);

        tagOutput.update(tagModel);

        verify(tagEntity).setSlug("changed-name");
    }

    @Test
    void update_nameSame_keepsSlug() {
        var tagId = UUID.randomUUID();
        var translations = Map.of(Language.ENGLISH, tagContentModel);
        when(tagModel.translations()).thenReturn(translations);
        when(tagModel.id()).thenReturn(tagId);
        when(tagContentModel.name()).thenReturn("Same Name");

        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getName()).thenReturn("Same Name");

        var contents = new ArrayList<>(List.of(existingContent));
        when(tagEntity.getContents()).thenReturn(contents);

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tagEntity));
        when(tagRepository.save(tagEntity)).thenReturn(tagEntity);
        when(tagOutputMapper.toModel(tagEntity)).thenReturn(expectedResult);

        tagOutput.update(tagModel);

        verify(tagEntity, never()).setSlug(anyString());
        verify(tagRepository, never()).countBySlugAndIdNot(anyString(), any());
    }

    @Test
    void deleteAll_delegatesToRepository() {
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        tagOutput.deleteAll(ids);

        verify(tagRepository).deleteByIdIn(ids);
    }

    @Test
    void search_withLanguage_filtersContentsInQuery_mapsWithPlainMapper() {
        Page<TagEntity> page = new PageImpl<>(List.of(tagEntity));

        when(tagRepository.search(any(PageRequest.class), eq("java"), eq(Language.ENGLISH))).thenReturn(page);
        when(tagOutputMapper.toModel(tagEntity)).thenReturn(expectedResult);

        var queryModel = new TagQueryModel("java", Language.ENGLISH);
        var input = new PaginatedInput<>(queryModel, 0, 10, "slug", Sort.Direction.ASC);

        var result = tagOutput.search(input, Language.ENGLISH);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(tagRepository).search(any(PageRequest.class), eq("java"), eq(Language.ENGLISH));
        verify(tagOutputMapper).toModel(tagEntity);
    }

    @Test
    void search_nullQuery_passesNullNameAndLanguageToRepository() {
        Page<TagEntity> page = new PageImpl<>(List.of());

        when(tagRepository.search(any(PageRequest.class), isNull(), eq(Language.ENGLISH))).thenReturn(page);

        var input = new PaginatedInput<>(new TagQueryModel(null, null), 0, 10, "createdAt", Sort.Direction.DESC);

        tagOutput.search(input, Language.ENGLISH);

        verify(tagRepository).search(any(PageRequest.class), isNull(), eq(Language.ENGLISH));
    }

    @Test
    void findAllByIdWithLanguage_usesSingleContentFetchAndOneArgMapper() {
        var id1 = UUID.randomUUID();
        var id2 = UUID.randomUUID();
        var tagEntity1 = mock(TagEntity.class);
        var tagEntity2 = mock(TagEntity.class);
        var tagModel1 = mock(TagModel.class);
        var tagModel2 = mock(TagModel.class);

        when(tagRepository.findWithSingleContent(List.of(id1, id2), Language.ENGLISH))
            .thenReturn(List.of(tagEntity1, tagEntity2));
        when(tagOutputMapper.toModel(tagEntity1)).thenReturn(tagModel1);
        when(tagOutputMapper.toModel(tagEntity2)).thenReturn(tagModel2);

        var result = tagOutput.findAllById(List.of(id1, id2), Language.ENGLISH);

        assertEquals(2, result.size());
        assertEquals(tagModel1, result.get(0));
        assertEquals(tagModel2, result.get(1));
        verify(tagRepository).findWithSingleContent(List.of(id1, id2), Language.ENGLISH);
        verify(tagRepository, never()).findAllById(anyList());
    }

    @Test
    void findAllByIdWithLanguage_nullIds_returnsEmptyList() {
        var result = tagOutput.findAllById(null, Language.ENGLISH);
        assertTrue(result.isEmpty());
        verifyNoInteractions(tagRepository);
    }

    @Test
    void findAllByIdWithLanguage_emptyIds_returnsEmptyList() {
        var result = tagOutput.findAllById(List.of(), Language.ENGLISH);
        assertTrue(result.isEmpty());
        verifyNoInteractions(tagRepository);
    }
}
