package dev.vitorpaulo.blog.output.project;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.domain.*;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.output.mapper.ProjectOutputMapper;
import dev.vitorpaulo.blog.repository.AuthorRepository;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import dev.vitorpaulo.blog.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectOutputTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectOutputMapper projectOutputMapper;
    @Mock private AuthorRepository authorRepository;
    @Mock private TagRepository tagRepository;
    @Mock private AuthorModel author;
    @Mock private ProjectModel project;
    @Mock private ProjectModel expectedResult;
    @Mock private ProjectEntity projectEntity;
    @Mock private ProjectContentEntity existingContent;
    @Mock private ProjectContentModel projectContentModel;

    @InjectMocks
    private ProjectOutput projectOutput;

    @BeforeEach
    void setUp() {
        lenient().when(author.id()).thenReturn(UUID.randomUUID());
        lenient().when(author.role()).thenReturn("org:admin");
    }

    @Test
    void findById_found_usesFindByIdWithContentsAndReturnsProject() {
        var entityId = UUID.randomUUID();

        when(projectRepository.findByIdWithContents(entityId)).thenReturn(Optional.of(projectEntity));
        when(projectOutputMapper.toModel(projectEntity)).thenReturn(expectedResult);

        var result = projectOutput.findById(entityId);

        assertEquals(expectedResult, result);
        verify(projectRepository).findByIdWithContents(entityId);
        verify(projectRepository, never()).findById(any());
    }

    @Test
    void findById_notFound_throwsNotFoundException() {
        when(projectRepository.findByIdWithContents(any())).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class, () -> projectOutput.findById(UUID.randomUUID()));
        assertEquals(ExceptionCode.PROJECT_NOT_FOUND, ex.getCode());
    }

    @Test
    void findBySlugAndIncrementView_found_incrementsViewCount() {
        when(projectRepository.findBySlugAndLanguage("my-project", Language.ENGLISH)).thenReturn(Optional.of(projectEntity));
        when(projectEntity.getViewCount()).thenReturn(5L);
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
        when(projectOutputMapper.toModel(projectEntity)).thenReturn(expectedResult);

        var result = projectOutput.findBySlugAndIncrementView("my-project", Language.ENGLISH);

        verify(projectEntity).setViewCount(6L);
        assertEquals(expectedResult, result);
        verify(projectRepository).save(projectEntity);
    }

    @Test
    void findBySlugAndIncrementView_nullViewCount_setsToOne() {
        when(projectRepository.findBySlugAndLanguage("my-project", Language.ENGLISH)).thenReturn(Optional.of(projectEntity));
        when(projectEntity.getViewCount()).thenReturn(null);
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
        when(projectOutputMapper.toModel(projectEntity)).thenReturn(expectedResult);

        projectOutput.findBySlugAndIncrementView("my-project", Language.ENGLISH);

        verify(projectEntity).setViewCount(1L);
    }

    @Test
    void findBySlugAndIncrementView_notFound_throwsNotFoundException() {
        when(projectRepository.findBySlugAndLanguage(anyString(), any())).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class,
                () -> projectOutput.findBySlugAndIncrementView("nonexistent", Language.ENGLISH));
        assertEquals(ExceptionCode.PROJECT_SLUG_NOT_FOUND, ex.getCode());
    }

    @Test
    void save_withEnglishTranslation_savesProject() {
        var translations = Map.of(Language.ENGLISH, projectContentModel);
        when(project.translations()).thenReturn(translations);
        when(projectContentModel.title()).thenReturn("My Project");

        var contentEntity = new ProjectContentEntity();
        when(projectOutputMapper.toContentEntity(projectContentModel)).thenReturn(contentEntity);

        when(projectRepository.countBySlugAndIdNot(anyString(), isNull())).thenReturn(0L);
        when(projectOutputMapper.toModel(any(ProjectEntity.class))).thenReturn(expectedResult);
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authorRepository.findAllById(anyList())).thenReturn(List.of());

        projectOutput.save(project, null, author);

        verify(projectRepository).save(any(ProjectEntity.class));
    }

    @Test
    void save_withConflict_generatesUniqueSlug() {
        var translations = Map.of(Language.ENGLISH, projectContentModel);
        when(project.translations()).thenReturn(translations);
        when(projectContentModel.title()).thenReturn("My Project");

        var contentEntity = new ProjectContentEntity();
        when(projectOutputMapper.toContentEntity(projectContentModel)).thenReturn(contentEntity);

        when(projectRepository.countBySlugAndIdNot("my-project", null)).thenReturn(2L);
        when(projectOutputMapper.toModel(any(ProjectEntity.class))).thenReturn(expectedResult);
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authorRepository.findAllById(anyList())).thenReturn(List.of());

        projectOutput.save(project, null, author);

        verify(projectRepository).save(argThat(e -> e.getSlug().equals("my-project-3")));
    }

    @Test
    void save_emptyTranslations_usesEmptyTitle() {
        when(project.translations()).thenReturn(Map.of());

        when(projectRepository.countBySlugAndIdNot(anyString(), isNull())).thenReturn(0L);
        when(projectOutputMapper.toModel(any(ProjectEntity.class))).thenReturn(expectedResult);
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authorRepository.findAllById(anyList())).thenReturn(List.of());

        projectOutput.save(project, null, author);

        verify(projectRepository).save(any(ProjectEntity.class));
    }

    @Test
    void save_nullTranslations_usesEmptyTitle() {
        when(project.translations()).thenReturn(null);

        when(projectRepository.countBySlugAndIdNot(anyString(), isNull())).thenReturn(0L);
        when(projectOutputMapper.toModel(any(ProjectEntity.class))).thenReturn(expectedResult);
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authorRepository.findAllById(anyList())).thenReturn(List.of());

        projectOutput.save(project, null, author);

        verify(projectRepository).save(any(ProjectEntity.class));
    }

    @Test
    void save_resetsReactionCounts() {
        var translations = Map.of(Language.ENGLISH, projectContentModel);
        when(project.translations()).thenReturn(translations);
        when(projectContentModel.title()).thenReturn("Test");

        var contentEntity = new ProjectContentEntity();
        when(projectOutputMapper.toContentEntity(projectContentModel)).thenReturn(contentEntity);

        when(projectRepository.countBySlugAndIdNot(anyString(), isNull())).thenReturn(0L);
        when(projectOutputMapper.toModel(any(ProjectEntity.class))).thenReturn(expectedResult);
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authorRepository.findAllById(anyList())).thenReturn(List.of());

        projectOutput.save(project, null, author);

        verify(projectRepository).save(argThat(e ->
            e.getViewCount() == 0L &&
            e.getLoveCount() == 0L &&
            e.getCelebrateCount() == 0L &&
            e.getGeniusCount() == 0L &&
            e.getHelpCount() == 0L
        ));
    }

    @Test
    void save_withTagIds_resolvesTags() {
        var translations = Map.of(Language.ENGLISH, projectContentModel);
        when(project.translations()).thenReturn(translations);
        when(projectContentModel.title()).thenReturn("Test");

        var contentEntity = new ProjectContentEntity();
        when(projectOutputMapper.toContentEntity(projectContentModel)).thenReturn(contentEntity);

        var tagIds = List.of(UUID.randomUUID());
        when(tagRepository.findAllById(tagIds)).thenReturn(List.of());

        when(projectRepository.countBySlugAndIdNot(anyString(), isNull())).thenReturn(0L);
        when(projectOutputMapper.toModel(any(ProjectEntity.class))).thenReturn(expectedResult);
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authorRepository.findAllById(anyList())).thenReturn(List.of());

        projectOutput.save(project, tagIds, author);

        verify(tagRepository).findAllById(tagIds);
    }

    @Test
    void update_notFound_throwsNotFoundException() {
        var projectId = UUID.randomUUID();
        when(project.id()).thenReturn(projectId);

        when(projectRepository.findByIdWithAuthor(projectId, author.id(), true)).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class,
                () -> projectOutput.update(project, null, author));
        assertEquals(ExceptionCode.PROJECT_NOT_FOUND, ex.getCode());
    }

    @Test
    void update_existingContent_updatesInPlace() {
        var projectId = UUID.randomUUID();
        var translations = Map.of(Language.ENGLISH, projectContentModel);
        when(project.translations()).thenReturn(translations);
        when(project.id()).thenReturn(projectId);
        when(projectContentModel.title()).thenReturn("New Title");
        when(projectContentModel.description()).thenReturn("New Description");

        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getTitle()).thenReturn("Old Title");

        var contents = new ArrayList<>(List.of(existingContent));
        when(projectEntity.getContents()).thenReturn(contents);

        when(projectRepository.findByIdWithAuthor(projectId, author.id(), true)).thenReturn(Optional.of(projectEntity));
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
        when(projectOutputMapper.toModel(projectEntity)).thenReturn(expectedResult);

        projectOutput.update(project, null, author);

        verify(existingContent).setTitle("New Title");
        verify(existingContent).setDescription("New Description");
        assertEquals(1, projectEntity.getContents().size());
    }

    @Test
    void update_newLanguage_addsAndRemovesOld() {
        var projectId = UUID.randomUUID();
        var translations = Map.of(Language.PORTUGUESE, projectContentModel);
        when(project.translations()).thenReturn(translations);
        when(project.id()).thenReturn(projectId);
        when(projectContentModel.title()).thenReturn("Titulo");

        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getTitle()).thenReturn("Old Title");

        var contents = new ArrayList<>(List.of(existingContent));
        when(projectEntity.getContents()).thenReturn(contents);

        var newContent = mock(ProjectContentEntity.class);
        when(newContent.getLanguage()).thenReturn(Language.PORTUGUESE);
        when(projectOutputMapper.toContentEntity(projectContentModel)).thenReturn(newContent);

        when(projectRepository.findByIdWithAuthor(projectId, author.id(), true)).thenReturn(Optional.of(projectEntity));
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
        when(projectOutputMapper.toModel(projectEntity)).thenReturn(expectedResult);

        projectOutput.update(project, null, author);

        verify(newContent).setLanguage(Language.PORTUGUESE);
        verify(newContent).setProject(projectEntity);
        assertEquals(1, contents.size());
    }

    @Test
    void update_titleChanged_regeneratesSlug() {
        var projectId = UUID.randomUUID();
        var translations = Map.of(Language.ENGLISH, projectContentModel);
        when(project.translations()).thenReturn(translations);
        when(project.id()).thenReturn(projectId);
        when(projectContentModel.title()).thenReturn("Changed Title");

        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getTitle()).thenReturn("Original Title");

        var contents = new ArrayList<>(List.of(existingContent));
        when(projectEntity.getId()).thenReturn(projectId);
        when(projectEntity.getContents()).thenReturn(contents);

        when(projectRepository.findByIdWithAuthor(projectId, author.id(), true)).thenReturn(Optional.of(projectEntity));
        when(projectRepository.countBySlugAndIdNot("changed-title", projectId)).thenReturn(0L);
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
        when(projectOutputMapper.toModel(projectEntity)).thenReturn(expectedResult);

        projectOutput.update(project, null, author);

        verify(projectEntity).setSlug("changed-title");
    }

    @Test
    void update_titleSame_keepsSlug() {
        var projectId = UUID.randomUUID();
        var translations = Map.of(Language.ENGLISH, projectContentModel);
        when(project.translations()).thenReturn(translations);
        when(project.id()).thenReturn(projectId);
        when(projectContentModel.title()).thenReturn("Same Title");

        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getTitle()).thenReturn("Same Title");

        var contents = new ArrayList<>(List.of(existingContent));
        when(projectEntity.getContents()).thenReturn(contents);

        when(projectRepository.findByIdWithAuthor(projectId, author.id(), true)).thenReturn(Optional.of(projectEntity));
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
        when(projectOutputMapper.toModel(projectEntity)).thenReturn(expectedResult);

        projectOutput.update(project, null, author);

        verify(projectEntity, never()).setSlug(anyString());
        verify(projectRepository, never()).countBySlugAndIdNot(anyString(), any());
    }

    @Test
    void update_nonAdmin_passesFalseFlag() {
        var projectId = UUID.randomUUID();
        when(author.id()).thenReturn(UUID.randomUUID());
        when(author.role()).thenReturn("org:member");

        var translations = Map.of(Language.ENGLISH, projectContentModel);
        when(project.translations()).thenReturn(translations);
        when(project.id()).thenReturn(projectId);
        when(projectContentModel.title()).thenReturn("Title");

        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getTitle()).thenReturn("Title");

        var contents = new ArrayList<>(List.of(existingContent));
        when(projectEntity.getContents()).thenReturn(contents);

        when(projectRepository.findByIdWithAuthor(projectId, author.id(), false)).thenReturn(Optional.of(projectEntity));
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
        when(projectOutputMapper.toModel(projectEntity)).thenReturn(expectedResult);

        projectOutput.update(project, null, author);

        verify(projectRepository).findByIdWithAuthor(projectId, author.id(), false);
    }

    @Test
    void deleteAll_withAuthor_delegatesToRepository() {
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        projectOutput.deleteAll(ids, author);

        verify(projectRepository).deleteByIdWithAuthor(ids, author.id(), true);
    }

    @Test
    void deleteAll_nonAdmin_passesFalseFlag() {
        var ids = List.of(UUID.randomUUID());
        when(author.id()).thenReturn(UUID.randomUUID());
        when(author.role()).thenReturn("org:member");

        projectOutput.deleteAll(ids, author);

        verify(projectRepository).deleteByIdWithAuthor(ids, author.id(), false);
    }

    @Test
    void search_withLanguage_returnsPaginatedResults() {
        var entityId = UUID.randomUUID();
        when(projectEntity.getId()).thenReturn(entityId);
        Page<ProjectEntity> page = new PageImpl<>(List.of(projectEntity));

        when(projectRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);
        when(projectRepository.findAllByIdWithSingleContent(List.of(entityId), Language.ENGLISH))
                .thenReturn(List.of(projectEntity));
        when(projectOutputMapper.toModel(projectEntity)).thenReturn(expectedResult);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(Language.ENGLISH);
        when(queryModel.tagId()).thenReturn(null);

        var input = new PaginatedInput<>(queryModel, 0, 10, "createdAt", Sort.Direction.DESC);

        var result = projectOutput.search(input, null);

        assertNotNull(result);
        assertEquals(1, result.content().size());
        verify(projectRepository).findAllByIdWithSingleContent(List.of(entityId), Language.ENGLISH);
    }

    @Test
    void search_emptyPage_skipsTranslationFetch() {
        Page<ProjectEntity> page = new PageImpl<>(List.of());

        when(projectRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(Language.ENGLISH);
        when(queryModel.tagId()).thenReturn(null);

        var input = new PaginatedInput<>(queryModel, 0, 10, "createdAt", Sort.Direction.DESC);

        var result = projectOutput.search(input, null);

        assertNotNull(result);
        assertTrue(result.content().isEmpty());
        verify(projectRepository, never()).findAllByIdWithSingleContent(anyList(), any());
    }

    @Test
    void search_nullLanguage_passesNullToRepository() {
        Page<ProjectEntity> page = new PageImpl<>(List.of());

        when(projectRepository.search(any(), any(), any(), isNull(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(null);
        when(queryModel.tagId()).thenReturn(null);

        var input = new PaginatedInput<>(queryModel, 0, 10, "createdAt", Sort.Direction.DESC);

        projectOutput.search(input, null);

        verify(projectRepository).search(isNull(), isNull(), isNull(), isNull(), eq(false), any(PageRequest.class), anyString());
    }

    @Test
    void search_viewCountSort_mapsCorrectly() {
        Page<ProjectEntity> page = new PageImpl<>(List.of());
        when(projectRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(Language.ENGLISH);
        when(queryModel.tagId()).thenReturn(null);

        var input = new PaginatedInput<>(queryModel, 0, 10, "viewCount", Sort.Direction.ASC);

        projectOutput.search(input, author);

        verify(projectRepository).search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), eq("view_count ASC"));
    }

    @Test
    void search_unknownField_defaultsToCreatedAtSort() {
        Page<ProjectEntity> page = new PageImpl<>(List.of());
        when(projectRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(Language.ENGLISH);
        when(queryModel.tagId()).thenReturn(null);

        var input = new PaginatedInput<>(queryModel, 0, 10, "unknownField", Sort.Direction.DESC);

        projectOutput.search(input, author);

        verify(projectRepository).search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), eq("created_at DESC, updated_at DESC"));
    }

    @Test
    void search_slugSort_mapsCorrectly() {
        Page<ProjectEntity> page = new PageImpl<>(List.of());
        when(projectRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(Language.ENGLISH);
        when(queryModel.tagId()).thenReturn(null);

        var input = new PaginatedInput<>(queryModel, 0, 10, "slug", Sort.Direction.ASC);

        projectOutput.search(input, author);

        verify(projectRepository).search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), eq("slug ASC"));
    }

    @Test
    void search_reactionCountSort_mapsCorrectly() {
        Page<ProjectEntity> page = new PageImpl<>(List.of());
        when(projectRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(Language.ENGLISH);
        when(queryModel.tagId()).thenReturn(null);

        var input = new PaginatedInput<>(queryModel, 0, 10, "reactionCount", Sort.Direction.DESC);

        projectOutput.search(input, author);

        verify(projectRepository).search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), eq("reaction_count DESC"));
    }

    @Test
    void findAllById_withIds_usesSingleContentFetchAndOneArgMapper() {
        var entityId = UUID.randomUUID();
        when(projectEntity.getId()).thenReturn(entityId);
        var ids = List.of(entityId);

        when(projectRepository.findAllByIdWithSingleContent(ids, Language.ENGLISH))
            .thenReturn(List.of(projectEntity));
        when(projectOutputMapper.toModel(projectEntity)).thenReturn(expectedResult);

        var result = projectOutput.findAllById(ids, Language.ENGLISH);

        assertEquals(1, result.size());
        assertEquals(expectedResult, result.getFirst());
        verify(projectRepository).findAllByIdWithSingleContent(ids, Language.ENGLISH);
        verify(projectRepository, never()).findAllById(anyList());
        verify(projectOutputMapper).toModel(projectEntity);
    }

    @Test
    void findAllById_nullIds_returnsEmptyList() {
        var result = projectOutput.findAllById(null, Language.ENGLISH);

        assertTrue(result.isEmpty());
        verifyNoInteractions(projectRepository);
    }

    @Test
    void findAllById_emptyIds_returnsEmptyList() {
        var result = projectOutput.findAllById(List.of(), Language.ENGLISH);

        assertTrue(result.isEmpty());
        verifyNoInteractions(projectRepository);
    }

    @Test
    void findAllByIdWithoutLanguage_withIds_returnsMappedResults() {
        var ids = List.of(UUID.randomUUID());
        when(projectRepository.findAllById(ids)).thenReturn(List.of(projectEntity));
        when(projectOutputMapper.toModel(projectEntity)).thenReturn(expectedResult);

        var result = projectOutput.findAllById(ids);

        assertEquals(1, result.size());
        assertEquals(expectedResult, result.getFirst());
    }

    @Test
    void findAllByIdWithoutLanguage_nullIds_returnsEmptyList() {
        var result = projectOutput.findAllById(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void search_withLanguage_fetchesSingleTranslationWithRequestedLanguage() {
        var entityId = UUID.randomUUID();
        when(projectEntity.getId()).thenReturn(entityId);
        Page<ProjectEntity> page = new PageImpl<>(List.of(projectEntity));

        when(projectRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);
        when(projectRepository.findAllByIdWithSingleContent(List.of(entityId), Language.PORTUGUESE))
                .thenReturn(List.of(projectEntity));
        when(projectOutputMapper.toModel(projectEntity)).thenReturn(expectedResult);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(Language.PORTUGUESE);
        when(queryModel.tagId()).thenReturn(null);

        var input = new PaginatedInput<>(queryModel, 0, 10, "createdAt", Sort.Direction.DESC);

        var result = projectOutput.search(input, null);

        assertEquals(List.of(expectedResult), result.content());
        verify(projectRepository).findAllByIdWithSingleContent(List.of(entityId), Language.PORTUGUESE);
        verify(projectOutputMapper).toModel(projectEntity);
    }

    @Test
    void search_nullLanguage_fetchesWithNullLanguageForQueryFallback() {
        var entityId = UUID.randomUUID();
        when(projectEntity.getId()).thenReturn(entityId);
        Page<ProjectEntity> page = new PageImpl<>(List.of(projectEntity));

        when(projectRepository.search(any(), any(), any(), isNull(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);
        when(projectRepository.findAllByIdWithSingleContent(List.of(entityId), null))
                .thenReturn(List.of(projectEntity));
        when(projectOutputMapper.toModel(projectEntity)).thenReturn(expectedResult);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(null);
        when(queryModel.tagId()).thenReturn(null);

        var input = new PaginatedInput<>(queryModel, 0, 10, "createdAt", Sort.Direction.DESC);

        var result = projectOutput.search(input, null);

        assertEquals(List.of(expectedResult), result.content());
        verify(projectRepository).findAllByIdWithSingleContent(List.of(entityId), null);
    }

    @Test
    void search_preservesPageOrderRegardlessOfFetchOrder() {
        var idA = UUID.randomUUID();
        var idB = UUID.randomUUID();
        var entityA = new ProjectEntity();
        entityA.setId(idA);
        entityA.setTags(new ArrayList<>());
        var entityB = new ProjectEntity();
        entityB.setId(idB);
        entityB.setTags(new ArrayList<>());

        Page<ProjectEntity> page = new PageImpl<>(List.of(entityA, entityB));
        when(projectRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);
        when(projectRepository.findAllByIdWithSingleContent(List.of(idA, idB), Language.ENGLISH))
                .thenReturn(List.of(entityB, entityA));
        when(projectOutputMapper.toModel(entityA)).thenReturn(expectedResult);
        when(projectOutputMapper.toModel(entityB)).thenReturn(project);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(Language.ENGLISH);
        when(queryModel.tagId()).thenReturn(null);

        var input = new PaginatedInput<>(queryModel, 0, 10, "createdAt", Sort.Direction.DESC);

        var result = projectOutput.search(input, null);

        assertEquals(List.of(expectedResult, project), result.content());
    }

    @Test
    void search_fetchesTagContentsForPageProjectsWithRequestedLanguage() {
        var entityId = UUID.randomUUID();
        when(projectEntity.getId()).thenReturn(entityId);
        Page<ProjectEntity> page = new PageImpl<>(List.of(projectEntity));

        when(projectRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);
        when(projectRepository.findAllByIdWithSingleContent(List.of(entityId), Language.PORTUGUESE))
                .thenReturn(List.of(projectEntity));
        when(projectOutputMapper.toModel(projectEntity)).thenReturn(expectedResult);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(Language.PORTUGUESE);
        when(queryModel.tagId()).thenReturn(null);

        var input = new PaginatedInput<>(queryModel, 0, 10, "createdAt", Sort.Direction.DESC);

        projectOutput.search(input, null);

    }

    @Test
    void search_fetchesTagContentsWithNullLanguageWhenNoLanguageRequested() {
        var entityId = UUID.randomUUID();
        when(projectEntity.getId()).thenReturn(entityId);
        Page<ProjectEntity> page = new PageImpl<>(List.of(projectEntity));

        when(projectRepository.search(any(), any(), any(), isNull(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);
        when(projectRepository.findAllByIdWithSingleContent(List.of(entityId), null))
                .thenReturn(List.of(projectEntity));
        when(projectOutputMapper.toModel(projectEntity)).thenReturn(expectedResult);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(null);
        when(queryModel.tagId()).thenReturn(null);

        var input = new PaginatedInput<>(queryModel, 0, 10, "createdAt", Sort.Direction.DESC);

        projectOutput.search(input, null);

    }

    @Test
    void findBySlugAndIncrementView_filtersContentsInQuery_mapsWithPlainMapper() {
        var ptContent = new ProjectContentEntity();
        ptContent.setLanguage(Language.PORTUGUESE);
        ptContent.setTitle("Titulo");
        ptContent.setDescription("Descricao");

        var entity = new ProjectEntity();
        entity.setContents(new ArrayList<>(List.of(ptContent)));
        entity.setTags(new ArrayList<>());
        entity.setViewCount(0L);

        when(projectRepository.findBySlugAndLanguage("my-project", Language.PORTUGUESE)).thenReturn(Optional.of(entity));
        when(projectRepository.save(entity)).thenReturn(entity);
        when(projectOutputMapper.toModel(entity)).thenReturn(expectedResult);

        var result = projectOutput.findBySlugAndIncrementView("my-project", Language.PORTUGUESE);

        assertEquals(expectedResult, result);
        verify(projectOutputMapper).toModel(entity);
    }
}
