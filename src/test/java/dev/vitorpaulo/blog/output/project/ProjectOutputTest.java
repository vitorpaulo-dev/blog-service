package dev.vitorpaulo.blog.output.project;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.domain.ProjectContentEntity;
import dev.vitorpaulo.blog.domain.ProjectEntity;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.output.mapper.ProjectOutputMapper;
import dev.vitorpaulo.blog.repository.AuthorRepository;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import dev.vitorpaulo.blog.repository.RedisRepository;
import dev.vitorpaulo.blog.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectOutputTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectOutputMapper projectOutputMapper;
    @Mock private AuthorRepository authorRepository;
    @Mock private TagRepository tagRepository;
    @Mock private RedisRepository redisRepository;
    @Mock private AuthorModel author;
    @Mock private ProjectModel project;
    @Mock private ProjectModel expectedResult;
    @Mock private ProjectModel secondResult;
    @Mock private ProjectEntity projectEntity;
    @Mock private ProjectEntity secondProjectEntity;
    @Mock private ProjectContentEntity existingContent;
    @Mock private ProjectContentEntity newContent;
    @Mock private ProjectContentModel projectContentModel;
    @Mock private ProjectQueryModel projectQueryModel;

    @InjectMocks
    private ProjectOutput projectOutput;

    @BeforeEach
    void setUp() {
        lenient().when(author.id()).thenReturn(UUID.randomUUID());
        lenient().when(author.role()).thenReturn("org:admin");
    }

    @Test
    void findById_found_mapsWithEmptyTagIds() {
        var id = UUID.randomUUID();
        when(projectRepository.findByIdWithContents(id)).thenReturn(Optional.of(projectEntity));
        when(projectOutputMapper.toModel(projectEntity, List.of())).thenReturn(expectedResult);

        var result = projectOutput.findById(id);

        assertEquals(expectedResult, result);
        verify(projectRepository).findByIdWithContents(id);
    }

    @Test
    void findById_notFound_throwsNotFoundException() {
        when(projectRepository.findByIdWithContents(any())).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class, () -> projectOutput.findById(UUID.randomUUID()));
        assertEquals(ExceptionCode.PROJECT_NOT_FOUND, ex.getCode());
    }

    @Test
    void findBySlugAndIncrementView_found_incrementsViewCount() {
        var projectId = UUID.randomUUID();
        when(projectRepository.findBySlugAndLanguage("my-project", Language.ENGLISH)).thenReturn(Optional.of(projectEntity));
        when(projectEntity.getId()).thenReturn(projectId);
        when(redisRepository.keyExists(eq("project:" + projectId + ":view:1.2.3.4"))).thenReturn(false);
        when(projectEntity.getViewCount()).thenReturn(5L);
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
        when(projectOutputMapper.toModel(projectEntity, List.of())).thenReturn(expectedResult);

        var result = projectOutput.findBySlugAndIncrementView("my-project", Language.ENGLISH, "1.2.3.4");

        verify(projectEntity).setViewCount(6L);
        assertEquals(expectedResult, result);
        verify(redisRepository).set(eq("project:" + projectId + ":view:1.2.3.4"), eq(Duration.ofHours(48)));
        verify(redisRepository).addToSortedSet(eq("project:" + projectId + ":views"), startsWith("1.2.3.4:"), anyDouble(), eq(Duration.ofHours(48)));
    }

    @Test
    void findBySlugAndIncrementView_nullViewCount_setsToOne() {
        var projectId = UUID.randomUUID();
        when(projectRepository.findBySlugAndLanguage("my-project", Language.ENGLISH)).thenReturn(Optional.of(projectEntity));
        when(projectEntity.getId()).thenReturn(projectId);
        when(redisRepository.keyExists(eq("project:" + projectId + ":view:1.2.3.4"))).thenReturn(false);
        when(projectEntity.getViewCount()).thenReturn(null);
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);

        projectOutput.findBySlugAndIncrementView("my-project", Language.ENGLISH, "1.2.3.4");

        verify(projectEntity).setViewCount(1L);
        verify(redisRepository).set(eq("project:" + projectId + ":view:1.2.3.4"), eq(Duration.ofHours(48)));
        verify(redisRepository).addToSortedSet(eq("project:" + projectId + ":views"), startsWith("1.2.3.4:"), anyDouble(), eq(Duration.ofHours(48)));
    }

    @Test
    void findBySlugAndIncrementView_keyAlreadyExists_doesNotIncrement() {
        var projectId = UUID.randomUUID();
        when(projectRepository.findBySlugAndLanguage("my-project", Language.ENGLISH)).thenReturn(Optional.of(projectEntity));
        when(projectEntity.getId()).thenReturn(projectId);
        when(redisRepository.keyExists("project:" + projectId + ":view:1.2.3.4")).thenReturn(true);

        projectOutput.findBySlugAndIncrementView("my-project", Language.ENGLISH, "1.2.3.4");

        verify(projectEntity, never()).setViewCount(anyLong());
        verify(projectRepository, never()).save(any());
        verify(redisRepository, never()).set(anyString(), any());
        verify(redisRepository, never()).addToSortedSet(anyString(), anyString(), anyDouble(), any());
    }

    @Test
    void findBySlugAndIncrementView_notFound_throwsNotFoundException() {
        when(projectRepository.findBySlugAndLanguage(anyString(), any())).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class,
                () -> projectOutput.findBySlugAndIncrementView("nonexistent", Language.ENGLISH, "1.2.3.4"));
        assertEquals(ExceptionCode.PROJECT_SLUG_NOT_FOUND, ex.getCode());
    }

    @ParameterizedTest
    @EnumSource(ReactionType.class)
    void react_keyAbsent_incrementsCountSavesAndSetsKey(ReactionType reactionType) {
        var projectId = UUID.randomUUID();
        stubReactionCount(reactionType, 1L);
        when(projectRepository.findBySlug("my-project")).thenReturn(Optional.of(projectEntity));
        when(projectEntity.getId()).thenReturn(projectId);
        when(redisRepository.keyExists(anyString())).thenReturn(false);
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
        when(projectOutputMapper.toReactionModel(projectEntity)).thenReturn(new ReactionModel(1L, 1L, 1L, 1L, 4L));

        var result = projectOutput.react("my-project", reactionType, "203.0.113.7");

        assertEquals(4L, result.reactionCount());
        assertReactionIncremented(reactionType, projectEntity);
        verify(redisRepository).set("project:" + projectId + ":reaction:203.0.113.7:" + reactionType, Duration.ofSeconds(604800));
        verify(redisRepository).addToSortedSet(eq("project:" + projectId + ":reactions"), startsWith("203.0.113.7:" + reactionType + ":"), anyDouble(), eq(Duration.ofSeconds(604800)));
    }

    @ParameterizedTest
    @EnumSource(ReactionType.class)
    void react_keyAlreadyPresent_returnsCountsUnchanged(ReactionType reactionType) {
        var projectId = UUID.randomUUID();
        var expected = new ReactionModel(1L, 2L, 3L, 4L, 10L);
        when(projectRepository.findBySlug("my-project")).thenReturn(Optional.of(projectEntity));
        when(projectEntity.getId()).thenReturn(projectId);
        when(redisRepository.keyExists("project:" + projectId + ":reaction:203.0.113.7:" + reactionType)).thenReturn(true);
        when(projectOutputMapper.toReactionModel(projectEntity)).thenReturn(expected);

        var result = projectOutput.react("my-project", reactionType, "203.0.113.7");

        assertEquals(expected, result);
        verify(projectRepository, never()).save(any());
        verify(redisRepository, never()).set(anyString(), any());
        verify(redisRepository, never()).addToSortedSet(anyString(), anyString(), anyDouble(), any());
    }

    @Test
    void react_unknownSlug_throwsNotFoundException() {
        when(projectRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class,
                () -> projectOutput.react("nonexistent", ReactionType.LOVE, "203.0.113.7"));
        assertEquals(ExceptionCode.PROJECT_NOT_FOUND, ex.getCode());
        verifyNoInteractions(redisRepository);
    }

    private void stubReactionCount(ReactionType reactionType, long current) {
        switch (reactionType) {
            case LOVE -> when(projectEntity.getLoveCount()).thenReturn(current);
            case CELEBRATE -> when(projectEntity.getCelebrateCount()).thenReturn(current);
            case GENIUS -> when(projectEntity.getGeniusCount()).thenReturn(current);
            case HELP -> when(projectEntity.getHelpCount()).thenReturn(current);
        }
    }

    private void assertReactionIncremented(ReactionType reactionType, ProjectEntity entity) {
        switch (reactionType) {
            case LOVE -> verify(entity).setLoveCount(2L);
            case CELEBRATE -> verify(entity).setCelebrateCount(2L);
            case GENIUS -> verify(entity).setGeniusCount(2L);
            case HELP -> verify(entity).setHelpCount(2L);
        }
    }

    @Test
    void save_validRequest_savesWithSlugFromTitle() {
        when(project.translations()).thenReturn(Map.of(Language.ENGLISH, projectContentModel));
        when(projectContentModel.title()).thenReturn("My Project");
        when(projectOutputMapper.toContentEntity(projectContentModel)).thenReturn(newContent);
        when(newContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(projectRepository.countBySlugAndIdNot("my-project", null)).thenReturn(0L);
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        projectOutput.save(project, null, author);

        verify(projectRepository).save(argThat(e -> "my-project".equals(e.getSlug())));
    }

    @Test
    void save_slugConflict_appendsIncrementedCounter() {
        when(project.translations()).thenReturn(Map.of(Language.ENGLISH, projectContentModel));
        when(projectContentModel.title()).thenReturn("My Project");
        when(projectOutputMapper.toContentEntity(projectContentModel)).thenReturn(newContent);
        when(newContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(projectRepository.countBySlugAndIdNot("my-project", null)).thenReturn(2L);
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        projectOutput.save(project, null, author);

        verify(projectRepository).save(argThat(e -> "my-project-3".equals(e.getSlug())));
    }

    @Test
    void save_nullTranslations_usesFallbackSlug() {
        when(project.translations()).thenReturn(null);
        when(projectRepository.countBySlugAndIdNot("post", null)).thenReturn(0L);
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        projectOutput.save(project, null, author);

        verify(projectRepository).save(argThat(e -> "post".equals(e.getSlug())));
    }

    @Test
    void save_withTagIds_resolvesTags() {
        when(project.translations()).thenReturn(Map.of(Language.ENGLISH, projectContentModel));
        when(projectContentModel.title()).thenReturn("My Project");
        when(projectOutputMapper.toContentEntity(projectContentModel)).thenReturn(newContent);
        when(newContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(projectRepository.countBySlugAndIdNot(anyString(), isNull())).thenReturn(0L);
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var tagIds = List.of(UUID.randomUUID());
        projectOutput.save(project, tagIds, author);

        verify(tagRepository).findAllById(tagIds);
    }

    @Test
    void update_notFound_throwsNotFoundException() {
        var projectId = UUID.randomUUID();
        when(project.id()).thenReturn(projectId);
        when(projectRepository.findByIdWithAuthor(projectId, author.id(), true)).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class, () -> projectOutput.update(project, null, author));
        assertEquals(ExceptionCode.PROJECT_NOT_FOUND, ex.getCode());
    }

    @Test
    void update_titleChanged_regeneratesSlug() {
        var projectId = UUID.randomUUID();
        when(project.translations()).thenReturn(Map.of(Language.ENGLISH, projectContentModel));
        when(project.id()).thenReturn(projectId);
        when(projectContentModel.title()).thenReturn("Changed Title");
        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getTitle()).thenReturn("Original Title");
        var contents = new ArrayList<>(List.of(existingContent));
        when(projectEntity.getContents()).thenReturn(contents);
        when(projectEntity.getId()).thenReturn(projectId);
        when(projectRepository.findByIdWithAuthor(projectId, author.id(), true)).thenReturn(Optional.of(projectEntity));
        when(projectRepository.countBySlugAndIdNot("changed-title", projectId)).thenReturn(0L);
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);

        projectOutput.update(project, null, author);

        verify(projectEntity).setSlug("changed-title");
    }

    @Test
    void update_titleUnchanged_keepsSlug() {
        var projectId = UUID.randomUUID();
        when(project.translations()).thenReturn(Map.of(Language.ENGLISH, projectContentModel));
        when(project.id()).thenReturn(projectId);
        when(projectContentModel.title()).thenReturn("Same Title");
        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getTitle()).thenReturn("Same Title");
        var contents = new ArrayList<>(List.of(existingContent));
        when(projectEntity.getContents()).thenReturn(contents);
        when(projectRepository.findByIdWithAuthor(projectId, author.id(), true)).thenReturn(Optional.of(projectEntity));
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);

        projectOutput.update(project, null, author);

        verify(projectEntity, never()).setSlug(anyString());
        verify(projectRepository, never()).countBySlugAndIdNot(anyString(), any());
    }

    @Test
    void update_existingTranslation_updatesInPlace() {
        var projectId = UUID.randomUUID();
        when(project.translations()).thenReturn(Map.of(Language.ENGLISH, projectContentModel));
        when(project.id()).thenReturn(projectId);
        when(projectContentModel.title()).thenReturn("New Title");
        when(projectContentModel.description()).thenReturn("New Description");
        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getTitle()).thenReturn("Old Title");
        var contents = new ArrayList<>(List.of(existingContent));
        when(projectEntity.getContents()).thenReturn(contents);
        when(projectRepository.findByIdWithAuthor(projectId, author.id(), true)).thenReturn(Optional.of(projectEntity));
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);

        projectOutput.update(project, null, author);

        verify(existingContent).setTitle("New Title");
        verify(existingContent).setDescription("New Description");
        assertEquals(1, contents.size());
    }

    @Test
    void update_newLanguage_addsTranslationAndRemovesOld() {
        var projectId = UUID.randomUUID();
        when(project.translations()).thenReturn(Map.of(Language.PORTUGUESE, projectContentModel));
        when(project.id()).thenReturn(projectId);
        when(projectContentModel.title()).thenReturn("Titulo");
        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getTitle()).thenReturn("Old Title");
        var contents = new ArrayList<>(List.of(existingContent));
        when(projectEntity.getContents()).thenReturn(contents);
        when(projectOutputMapper.toContentEntity(projectContentModel)).thenReturn(newContent);
        when(newContent.getLanguage()).thenReturn(Language.PORTUGUESE);
        when(projectRepository.findByIdWithAuthor(projectId, author.id(), true)).thenReturn(Optional.of(projectEntity));
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);

        projectOutput.update(project, null, author);

        verify(newContent).setLanguage(Language.PORTUGUESE);
        verify(newContent).setProject(projectEntity);
        assertEquals(1, contents.size());
        assertSame(newContent, contents.getFirst());
    }

    @Test
    void update_nonAdmin_requiresOwnership() {
        var projectId = UUID.randomUUID();
        when(author.id()).thenReturn(UUID.randomUUID());
        when(author.role()).thenReturn("org:member");
        when(project.translations()).thenReturn(Map.of(Language.ENGLISH, projectContentModel));
        when(project.id()).thenReturn(projectId);
        when(projectContentModel.title()).thenReturn("Title");
        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getTitle()).thenReturn("Title");
        var contents = new ArrayList<>(List.of(existingContent));
        when(projectEntity.getContents()).thenReturn(contents);
        when(projectRepository.findByIdWithAuthor(projectId, author.id(), false)).thenReturn(Optional.of(projectEntity));
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);

        projectOutput.update(project, null, author);

        verify(projectRepository).findByIdWithAuthor(projectId, author.id(), false);
    }

    @Test
    void deleteAll_admin_bypassesOwnershipCheck() {
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        projectOutput.deleteAll(ids, author);

        verify(projectRepository).deleteByIdWithAuthor(ids, author.id(), true);
    }

    @Test
    void deleteAll_nonAdmin_requiresOwnership() {
        var ids = List.of(UUID.randomUUID());
        when(author.id()).thenReturn(UUID.randomUUID());
        when(author.role()).thenReturn("org:member");

        projectOutput.deleteAll(ids, author);

        verify(projectRepository).deleteByIdWithAuthor(ids, author.id(), false);
    }

    @Test
    void search_propagatesLanguageAndShowDraftsToRepository() {
        when(projectQueryModel.query()).thenReturn("spring");
        when(projectQueryModel.language()).thenReturn(Language.ENGLISH);
        when(projectRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(new PageImpl<>(List.of()));

        projectOutput.search(input(projectQueryModel, "createdAt", Sort.Direction.DESC), null);

        verify(projectRepository).search(eq("spring"), isNull(), isNull(), eq("ENGLISH"), eq(false),
                any(PageRequest.class), eq("created_at DESC, updated_at DESC"));
    }

    @Test
    void search_nullLanguage_passesNullToRepository() {
        when(projectQueryModel.language()).thenReturn(null);
        when(projectRepository.search(any(), any(), any(), isNull(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(new PageImpl<>(List.of()));

        projectOutput.search(input(projectQueryModel, "createdAt", Sort.Direction.DESC), null);

        verify(projectRepository).search(any(), any(), any(), isNull(), eq(false),
                any(PageRequest.class), anyString());
    }

    @Test
    void search_withAuthor_showsDrafts() {
        when(projectQueryModel.language()).thenReturn(Language.ENGLISH);
        when(projectRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(new PageImpl<>(List.of()));

        projectOutput.search(input(projectQueryModel, "createdAt", Sort.Direction.DESC), author);

        verify(projectRepository).search(any(), any(), any(), any(), eq(true),
                any(PageRequest.class), anyString());
    }

    @Test
    void search_mapsSortProperty_viewCount() {
        when(projectQueryModel.language()).thenReturn(Language.ENGLISH);
        when(projectRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(new PageImpl<>(List.of()));

        projectOutput.search(input(projectQueryModel, "viewCount", Sort.Direction.ASC), null);

        verify(projectRepository).search(any(), any(), any(), any(), eq(false),
                any(PageRequest.class), eq("view_count ASC"));
    }

    @Test
    void search_mapsSortProperty_slug() {
        when(projectQueryModel.language()).thenReturn(Language.ENGLISH);
        when(projectRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(new PageImpl<>(List.of()));

        projectOutput.search(input(projectQueryModel, "slug", Sort.Direction.DESC), null);

        verify(projectRepository).search(any(), any(), any(), any(), eq(false),
                any(PageRequest.class), eq("slug DESC"));
    }

    @Test
    void search_unknownSortField_defaultsToCreatedAt() {
        when(projectQueryModel.language()).thenReturn(Language.ENGLISH);
        when(projectRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(new PageImpl<>(List.of()));

        projectOutput.search(input(projectQueryModel, "unknownField", Sort.Direction.DESC), null);

        verify(projectRepository).search(any(), any(), any(), any(), eq(false),
                any(PageRequest.class), eq("created_at DESC, updated_at DESC"));
    }

    @Test
    void search_emptyPage_returnsEmptyContent() {
        when(projectQueryModel.language()).thenReturn(Language.ENGLISH);
        when(projectRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(new PageImpl<>(List.of()));

        var result = projectOutput.search(input(projectQueryModel, "createdAt", Sort.Direction.DESC), null);

        assertTrue(result.content().isEmpty());
        verifyNoInteractions(projectOutputMapper);
    }

    @Test
    void search_preservesPageOrder() {
        when(projectOutputMapper.toModel(projectEntity, List.of())).thenReturn(expectedResult);
        when(projectOutputMapper.toModel(secondProjectEntity, List.of())).thenReturn(secondResult);
        when(expectedResult.translations()).thenReturn(Map.of());
        when(secondResult.translations()).thenReturn(Map.of());
        when(projectQueryModel.language()).thenReturn(Language.ENGLISH);
        when(projectRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(new PageImpl<>(List.of(projectEntity, secondProjectEntity)));

        var result = projectOutput.search(input(projectQueryModel, "createdAt", Sort.Direction.DESC), null);

        assertEquals(List.of(expectedResult, secondResult), result.content());
    }

    // Committed behavior: search filters non-requested languages in Java (removeIf).
    @Test
    void search_filtersOtherLanguagesInJava() {
        var translations = new HashMap<Language, ProjectContentModel>();
        translations.put(Language.ENGLISH, null);
        translations.put(Language.PORTUGUESE, null);
        when(projectOutputMapper.toModel(eq(projectEntity), anyList())).thenReturn(expectedResult);
        when(expectedResult.translations()).thenReturn(translations);
        when(projectQueryModel.language()).thenReturn(Language.ENGLISH);
        when(projectRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(new PageImpl<>(List.of(projectEntity)));

        var result = projectOutput.search(input(projectQueryModel, "createdAt", Sort.Direction.DESC), null);

        assertEquals(1, result.content().size());
        assertTrue(translations.containsKey(Language.ENGLISH));
        assertFalse(translations.containsKey(Language.PORTUGUESE));
    }

    @Test
    void search_isTransactionalReadOnly() throws NoSuchMethodException {
        Method method = ProjectOutput.class.getMethod("search", PaginatedInput.class, AuthorModel.class);
        var transactional = method.getAnnotation(Transactional.class);

        assertNotNull(transactional);
        assertTrue(transactional.readOnly());
    }

    @Test
    void findAllById_usesSingleContentFetch() {
        var id = UUID.randomUUID();
        when(projectRepository.findAllByIdWithSingleContent(List.of(id), Language.ENGLISH)).thenReturn(List.of(projectEntity));
        when(projectOutputMapper.toModel(projectEntity, List.of())).thenReturn(expectedResult);
        when(expectedResult.translations()).thenReturn(Map.of());

        var result = projectOutput.findAllById(List.of(id), Language.ENGLISH);

        assertEquals(List.of(expectedResult), result);
        verify(projectRepository, never()).findAllById(anyList());
    }

    @Test
    void findAllById_nullIds_returnsEmptyList() {
        var result = projectOutput.findAllById(null, Language.ENGLISH);

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllById_emptyIds_returnsEmptyList() {
        var result = projectOutput.findAllById(List.of(), Language.ENGLISH);

        assertTrue(result.isEmpty());
    }

    // Committed behavior: batch filters non-requested languages in Java (removeIf).
    @Test
    void findAllById_filtersOtherLanguagesInJava() {
        var translations = new HashMap<Language, ProjectContentModel>();
        translations.put(Language.PORTUGUESE, null);
        translations.put(Language.ENGLISH, null);
        when(projectRepository.findAllByIdWithSingleContent(anyList(), eq(Language.PORTUGUESE))).thenReturn(List.of(projectEntity));
        when(projectOutputMapper.toModel(eq(projectEntity), anyList())).thenReturn(expectedResult);
        when(expectedResult.translations()).thenReturn(translations);

        var result = projectOutput.findAllById(List.of(UUID.randomUUID()), Language.PORTUGUESE);

        assertEquals(1, result.size());
        assertTrue(translations.containsKey(Language.PORTUGUESE));
        assertFalse(translations.containsKey(Language.ENGLISH));
    }

    @Test
    void findAllById_isTransactionalReadOnly() throws NoSuchMethodException {
        Method method = ProjectOutput.class.getMethod("findAllById", List.class, Language.class);
        var transactional = method.getAnnotation(Transactional.class);

        assertNotNull(transactional);
        assertTrue(transactional.readOnly());
    }

    private PaginatedInput<ProjectQueryModel> input(ProjectQueryModel query, String sort, Sort.Direction direction) {
        return new PaginatedInput<>(query, 0, 10, sort, direction);
    }
}
