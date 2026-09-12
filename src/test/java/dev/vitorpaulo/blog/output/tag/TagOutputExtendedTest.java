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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    void save_validRequest_savesWithSlugFromName() {
        when(tagModel.translations()).thenReturn(Map.of(Language.ENGLISH, tagContentModel));
        when(tagContentModel.name()).thenReturn("Java");
        when(tagOutputMapper.toContentEntity(tagContentModel)).thenReturn(new TagContentEntity());
        when(tagRepository.countBySlugAndIdNot("java", null)).thenReturn(0L);
        when(tagRepository.save(any(TagEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        tagOutput.save(tagModel);

        verify(tagRepository).save(argThat(e -> "java".equals(e.getSlug())));
    }

    @Test
    void save_slugConflict_appendsIncrementedCounter() {
        when(tagModel.translations()).thenReturn(Map.of(Language.ENGLISH, tagContentModel));
        when(tagContentModel.name()).thenReturn("Java");
        when(tagOutputMapper.toContentEntity(tagContentModel)).thenReturn(new TagContentEntity());
        when(tagRepository.countBySlugAndIdNot("java", null)).thenReturn(2L);
        when(tagRepository.save(any(TagEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        tagOutput.save(tagModel);

        verify(tagRepository).save(argThat(e -> "java-3".equals(e.getSlug())));
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
    void update_nameChanged_regeneratesSlug() {
        var tagId = UUID.randomUUID();
        when(tagModel.translations()).thenReturn(Map.of(Language.ENGLISH, tagContentModel));
        when(tagModel.id()).thenReturn(tagId);
        when(tagContentModel.name()).thenReturn("Changed Name");
        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getName()).thenReturn("Original Name");
        var contents = new ArrayList<>(List.of(existingContent));
        when(tagEntity.getContents()).thenReturn(contents);
        when(tagEntity.getId()).thenReturn(tagId);
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tagEntity));
        when(tagRepository.countBySlugAndIdNot("changed-name", tagId)).thenReturn(0L);
        when(tagRepository.save(tagEntity)).thenReturn(tagEntity);

        tagOutput.update(tagModel);

        verify(tagEntity).setSlug("changed-name");
    }

    @Test
    void update_nameUnchanged_keepsSlug() {
        var tagId = UUID.randomUUID();
        when(tagModel.translations()).thenReturn(Map.of(Language.ENGLISH, tagContentModel));
        when(tagModel.id()).thenReturn(tagId);
        when(tagContentModel.name()).thenReturn("Same Name");
        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getName()).thenReturn("Same Name");
        var contents = new ArrayList<>(List.of(existingContent));
        when(tagEntity.getContents()).thenReturn(contents);
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tagEntity));
        when(tagRepository.save(tagEntity)).thenReturn(tagEntity);

        tagOutput.update(tagModel);

        verify(tagEntity, never()).setSlug(anyString());
        verify(tagRepository, never()).countBySlugAndIdNot(anyString(), any());
    }

    @Test
    void update_existingTranslation_updatesInPlace() {
        var tagId = UUID.randomUUID();
        when(tagModel.translations()).thenReturn(Map.of(Language.ENGLISH, tagContentModel));
        when(tagModel.id()).thenReturn(tagId);
        when(tagContentModel.name()).thenReturn("New Name");
        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getName()).thenReturn("Old Name");
        var contents = new ArrayList<>(List.of(existingContent));
        when(tagEntity.getContents()).thenReturn(contents);
        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tagEntity));
        when(tagRepository.save(tagEntity)).thenReturn(tagEntity);

        tagOutput.update(tagModel);

        verify(existingContent).setName("New Name");
        assertEquals(1, contents.size());
    }

    @Test
    void deleteAll_delegatesToRepository() {
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        tagOutput.deleteAll(ids);

        verify(tagRepository).deleteByIdIn(ids);
    }

    @Test
    void search_propagatesNameAndLanguageToRepository() {
        when(tagRepository.search(any(), any(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        var input = new PaginatedInput<>(new TagQueryModel("java", Language.ENGLISH), 0, 10, "slug", Sort.Direction.ASC);

        tagOutput.search(input, Language.ENGLISH);

        verify(tagRepository).search(eq("java"), eq(Language.ENGLISH), any(PageRequest.class));
    }

    @Test
    void search_nullQuery_passesNullName() {
        when(tagRepository.search(isNull(), eq(Language.ENGLISH), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        var input = new PaginatedInput<TagQueryModel>(null, 0, 10, "createdAt", Sort.Direction.DESC);

        tagOutput.search(input, Language.ENGLISH);

        verify(tagRepository).search(isNull(), eq(Language.ENGLISH), any(PageRequest.class));
    }

    @Test
    void search_mapsSortPropertyAndDirectionIntoPageable() {
        when(tagRepository.search(any(), any(), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of()));

        var input = new PaginatedInput<>(new TagQueryModel(null, Language.ENGLISH), 0, 10, "name", Sort.Direction.DESC);

        tagOutput.search(input, Language.ENGLISH);

        var captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(tagRepository).search(any(), any(), captor.capture());
        assertEquals(Sort.by(Sort.Direction.DESC, "slug"), captor.getValue().getSort());
    }

    @Test
    void search_unknownSortField_defaultsToCreatedAt() {
        when(tagRepository.search(any(), any(), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of()));

        var input = new PaginatedInput<>(new TagQueryModel(null, Language.ENGLISH), 0, 10, "unknown", Sort.Direction.ASC);

        tagOutput.search(input, Language.ENGLISH);

        var captor = ArgumentCaptor.forClass(PageRequest.class);
        verify(tagRepository).search(any(), any(), captor.capture());
        assertEquals(Sort.by(Sort.Direction.ASC, "created_at"), captor.getValue().getSort());
    }

    @Test
    void search_mapsWithPlainMapperAndFiltersOtherLanguagesInJava() {
        var translations = new java.util.HashMap<Language, dev.vitorpaulo.blog.model.TagContentModel>();
        translations.put(Language.ENGLISH, null);
        translations.put(Language.PORTUGUESE, null);
        when(tagRepository.search(any(), any(), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(tagEntity)));
        when(tagOutputMapper.toModel(tagEntity)).thenReturn(expectedResult);
        when(expectedResult.translations()).thenReturn(translations);

        var input = new PaginatedInput<>(new TagQueryModel("java", Language.ENGLISH), 0, 10, "slug", Sort.Direction.ASC);

        var result = tagOutput.search(input, Language.ENGLISH);

        assertEquals(List.of(expectedResult), result.content());
        assertTrue(translations.containsKey(Language.ENGLISH));
        assertFalse(translations.containsKey(Language.PORTUGUESE));
    }
}
