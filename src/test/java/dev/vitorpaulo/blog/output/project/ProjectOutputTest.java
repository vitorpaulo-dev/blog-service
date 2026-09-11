package dev.vitorpaulo.blog.output.project;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.domain.ProjectContentEntity;
import dev.vitorpaulo.blog.domain.ProjectEntity;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.output.mapper.ProjectMapper;
import dev.vitorpaulo.blog.repository.AuthorRepository;
import dev.vitorpaulo.blog.repository.ProjectRepository;
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
    @Mock private ProjectMapper projectMapper;
    @Mock private AuthorRepository authorRepository;
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
    void findById_found_returnsProject() {
        var entityId = UUID.randomUUID();

        when(projectRepository.findById(entityId)).thenReturn(Optional.of(projectEntity));
        when(projectMapper.toModel(projectEntity)).thenReturn(expectedResult);

        var result = projectOutput.findById(entityId);

        assertEquals(expectedResult, result);
    }

    @Test
    void findById_notFound_throwsNotFoundException() {
        when(projectRepository.findById(any())).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class, () -> projectOutput.findById(UUID.randomUUID()));
        assertEquals(ExceptionCode.PROJECT_NOT_FOUND, ex.getCode());
    }

    @Test
    void findBySlugAndIncrementView_found_incrementsViewCount() {
        when(projectRepository.findBySlugAndLanguage("my-project", Language.ENGLISH)).thenReturn(Optional.of(projectEntity));
        when(projectEntity.getViewCount()).thenReturn(5L);
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
        when(projectMapper.toModel(projectEntity, Language.ENGLISH)).thenReturn(expectedResult);

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
        when(projectMapper.toModel(projectEntity, Language.ENGLISH)).thenReturn(expectedResult);

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
        when(projectMapper.toContentEntity(projectContentModel)).thenReturn(contentEntity);

        when(projectRepository.countBySlugAndIdNot(anyString(), isNull())).thenReturn(0L);
        when(projectMapper.toModel(any(ProjectEntity.class))).thenReturn(expectedResult);
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authorRepository.findAllById(anyList())).thenReturn(List.of());

        projectOutput.save(project, author);

        verify(projectRepository).save(any(ProjectEntity.class));
    }

    @Test
    void save_withConflict_generatesUniqueSlug() {
        var translations = Map.of(Language.ENGLISH, projectContentModel);
        when(project.translations()).thenReturn(translations);
        when(projectContentModel.title()).thenReturn("My Project");

        var contentEntity = new ProjectContentEntity();
        when(projectMapper.toContentEntity(projectContentModel)).thenReturn(contentEntity);

        when(projectRepository.countBySlugAndIdNot("my-project", null)).thenReturn(2L);
        when(projectMapper.toModel(any(ProjectEntity.class))).thenReturn(expectedResult);
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authorRepository.findAllById(anyList())).thenReturn(List.of());

        projectOutput.save(project, author);

        verify(projectRepository).save(argThat(e -> e.getSlug().equals("my-project-3")));
    }

    @Test
    void save_emptyTranslations_usesEmptyTitle() {
        when(project.translations()).thenReturn(Map.of());

        when(projectRepository.countBySlugAndIdNot(anyString(), isNull())).thenReturn(0L);
        when(projectMapper.toModel(any(ProjectEntity.class))).thenReturn(expectedResult);
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authorRepository.findAllById(anyList())).thenReturn(List.of());

        projectOutput.save(project, author);

        verify(projectRepository).save(any(ProjectEntity.class));
    }

    @Test
    void save_nullTranslations_usesEmptyTitle() {
        when(project.translations()).thenReturn(null);

        when(projectRepository.countBySlugAndIdNot(anyString(), isNull())).thenReturn(0L);
        when(projectMapper.toModel(any(ProjectEntity.class))).thenReturn(expectedResult);
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authorRepository.findAllById(anyList())).thenReturn(List.of());

        projectOutput.save(project, author);

        verify(projectRepository).save(any(ProjectEntity.class));
    }

    @Test
    void save_resetsReactionCounts() {
        var translations = Map.of(Language.ENGLISH, projectContentModel);
        when(project.translations()).thenReturn(translations);
        when(projectContentModel.title()).thenReturn("Test");

        var contentEntity = new ProjectContentEntity();
        when(projectMapper.toContentEntity(projectContentModel)).thenReturn(contentEntity);

        when(projectRepository.countBySlugAndIdNot(anyString(), isNull())).thenReturn(0L);
        when(projectMapper.toModel(any(ProjectEntity.class))).thenReturn(expectedResult);
        when(projectRepository.save(any(ProjectEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authorRepository.findAllById(anyList())).thenReturn(List.of());

        projectOutput.save(project, author);

        verify(projectRepository).save(argThat(e ->
            e.getViewCount() == 0L &&
            e.getLoveCount() == 0L &&
            e.getCelebrateCount() == 0L &&
            e.getGeniusCount() == 0L &&
            e.getHelpCount() == 0L
        ));
    }

    @Test
    void update_notFound_throwsNotFoundException() {
        var projectId = UUID.randomUUID();
        when(project.id()).thenReturn(projectId);

        when(projectRepository.findByIdWithAuthor(projectId, author.id(), true)).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class,
                () -> projectOutput.update(project, author));
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
        when(projectMapper.toModel(projectEntity)).thenReturn(expectedResult);

        projectOutput.update(project, author);

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
        when(projectMapper.toContentEntity(projectContentModel)).thenReturn(newContent);

        when(projectRepository.findByIdWithAuthor(projectId, author.id(), true)).thenReturn(Optional.of(projectEntity));
        when(projectRepository.save(projectEntity)).thenReturn(projectEntity);
        when(projectMapper.toModel(projectEntity)).thenReturn(expectedResult);

        projectOutput.update(project, author);

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
        when(projectMapper.toModel(projectEntity)).thenReturn(expectedResult);

        projectOutput.update(project, author);

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
        when(projectMapper.toModel(projectEntity)).thenReturn(expectedResult);

        projectOutput.update(project, author);

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
        when(projectMapper.toModel(projectEntity)).thenReturn(expectedResult);

        projectOutput.update(project, author);

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
        Page<ProjectEntity> page = new PageImpl<>(List.of(projectEntity));

        when(projectRepository.search(any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);
        when(projectMapper.toModel(eq(projectEntity), eq(Language.ENGLISH))).thenReturn(expectedResult);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(Language.ENGLISH);

        var input = new PaginatedInput<>(queryModel, 0, 10, "createdAt", Sort.Direction.DESC);

        var result = projectOutput.search(input, null);

        assertNotNull(result);
        assertEquals(1, result.content().size());
    }

    @Test
    void search_nullLanguage_passesNullToRepository() {
        Page<ProjectEntity> page = new PageImpl<>(List.of());

        when(projectRepository.search(any(), any(), isNull(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(null);

        var input = new PaginatedInput<>(queryModel, 0, 10, "createdAt", Sort.Direction.DESC);

        projectOutput.search(input, null);

        verify(projectRepository).search(isNull(), isNull(), isNull(), eq(false), any(PageRequest.class), anyString());
    }

    @Test
    void search_viewCountSort_mapsCorrectly() {
        Page<ProjectEntity> page = new PageImpl<>(List.of());
        when(projectRepository.search(any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(Language.ENGLISH);

        var input = new PaginatedInput<>(queryModel, 0, 10, "viewCount", Sort.Direction.ASC);

        projectOutput.search(input, author);

        verify(projectRepository).search(any(), any(), any(), anyBoolean(), any(PageRequest.class), eq("view_count ASC"));
    }

    @Test
    void search_unknownField_defaultsToCreatedAtSort() {
        Page<ProjectEntity> page = new PageImpl<>(List.of());
        when(projectRepository.search(any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(Language.ENGLISH);

        var input = new PaginatedInput<>(queryModel, 0, 10, "unknownField", Sort.Direction.DESC);

        projectOutput.search(input, author);

        verify(projectRepository).search(any(), any(), any(), anyBoolean(), any(PageRequest.class), eq("created_at DESC, updated_at DESC"));
    }

    @Test
    void search_slugSort_mapsCorrectly() {
        Page<ProjectEntity> page = new PageImpl<>(List.of());
        when(projectRepository.search(any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(Language.ENGLISH);

        var input = new PaginatedInput<>(queryModel, 0, 10, "slug", Sort.Direction.ASC);

        projectOutput.search(input, author);

        verify(projectRepository).search(any(), any(), any(), anyBoolean(), any(PageRequest.class), eq("slug ASC"));
    }

    @Test
    void search_reactionCountSort_mapsCorrectly() {
        Page<ProjectEntity> page = new PageImpl<>(List.of());
        when(projectRepository.search(any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);

        var queryModel = mock(ProjectQueryModel.class);
        when(queryModel.language()).thenReturn(Language.ENGLISH);

        var input = new PaginatedInput<>(queryModel, 0, 10, "reactionCount", Sort.Direction.DESC);

        projectOutput.search(input, author);

        verify(projectRepository).search(any(), any(), any(), anyBoolean(), any(PageRequest.class), eq("reaction_count DESC"));
    }

    @Test
    void findAllById_withIds_returnsMappedResults() {
        var ids = List.of(UUID.randomUUID());
        when(projectRepository.findAllById(ids)).thenReturn(List.of(projectEntity));
        when(projectMapper.toModel(projectEntity, Language.ENGLISH)).thenReturn(expectedResult);

        var result = projectOutput.findAllById(ids, Language.ENGLISH);

        assertEquals(1, result.size());
        assertEquals(expectedResult, result.getFirst());
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

    @Test
    void findAllByIdWithoutLanguage_withIds_returnsMappedResults() {
        var ids = List.of(UUID.randomUUID());
        when(projectRepository.findAllById(ids)).thenReturn(List.of(projectEntity));
        when(projectMapper.toModel(projectEntity)).thenReturn(expectedResult);

        var result = projectOutput.findAllById(ids);

        assertEquals(1, result.size());
        assertEquals(expectedResult, result.getFirst());
    }

    @Test
    void findAllByIdWithoutLanguage_nullIds_returnsEmptyList() {
        var result = projectOutput.findAllById(null);

        assertTrue(result.isEmpty());
    }
}
