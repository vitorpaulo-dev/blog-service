package dev.vitorpaulo.blog.output.post;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.domain.PostContentEntity;
import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.output.mapper.PostOutputMapper;
import dev.vitorpaulo.blog.repository.AuthorRepository;
import dev.vitorpaulo.blog.repository.PostRepository;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import dev.vitorpaulo.blog.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostOutputTest {

    @Mock private PostRepository postRepository;
    @Mock private PostOutputMapper postOutputMapper;
    @Mock private ProjectRepository projectRepository;
    @Mock private TagRepository tagRepository;
    @Mock private AuthorRepository authorRepository;
    @Mock private PostModel post;
    @Mock private PostModel expectedResult;
    @Mock private PostModel secondResult;
    @Mock private PostEntity postEntity;
    @Mock private PostEntity secondPostEntity;
    @Mock private PostContentEntity existingContent;
    @Mock private PostContentEntity newContent;
    @Mock private PostContentModel postContentModel;
    @Mock private AuthorModel author;
    @Mock private PostQueryModel postQueryModel;

    @InjectMocks
    private PostOutput postOutput;

    @BeforeEach
    void setUp() {
        lenient().when(author.id()).thenReturn(UUID.randomUUID());
        lenient().when(author.role()).thenReturn("org:admin");
    }

    @Test
    void findById_found_mapsWithProjectAndTagIds() {
        var id = UUID.randomUUID();
        when(postRepository.findByIdWithContents(id)).thenReturn(Optional.of(postEntity));
        when(postOutputMapper.toModel(eq(postEntity), anyList(), anyList())).thenReturn(expectedResult);

        var result = postOutput.findById(id);

        assertEquals(expectedResult, result);
        verify(postRepository).findProjectIds(id);
        verify(postRepository).findTagIds(id);
    }

    @Test
    void findById_notFound_throwsNotFoundException() {
        when(postRepository.findByIdWithContents(any())).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class, () -> postOutput.findById(UUID.randomUUID()));
        assertEquals(ExceptionCode.POST_NOT_FOUND, ex.getCode());
    }

    @Test
    void findBySlugAndIncrementView_found_incrementsViewCount() {
        when(postRepository.findBySlugAndLanguage("my-post", Language.ENGLISH)).thenReturn(Optional.of(postEntity));
        when(postEntity.getViewCount()).thenReturn(5L);
        when(postRepository.save(postEntity)).thenReturn(postEntity);

        postOutput.findBySlugAndIncrementView("my-post", Language.ENGLISH);

        verify(postEntity).setViewCount(6L);
        verify(postOutputMapper).toModel(eq(postEntity), anyList(), anyList());
    }

    @Test
    void findBySlugAndIncrementView_nullViewCount_setsToOne() {
        when(postRepository.findBySlugAndLanguage("my-post", Language.ENGLISH)).thenReturn(Optional.of(postEntity));
        when(postEntity.getViewCount()).thenReturn(null);
        when(postRepository.save(postEntity)).thenReturn(postEntity);

        postOutput.findBySlugAndIncrementView("my-post", Language.ENGLISH);

        verify(postEntity).setViewCount(1L);
    }

    @Test
    void findBySlugAndIncrementView_notFound_throwsNotFoundException() {
        when(postRepository.findBySlugAndLanguage(anyString(), any())).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class,
                () -> postOutput.findBySlugAndIncrementView("nonexistent", Language.ENGLISH));
        assertEquals(ExceptionCode.POST_SLUG_NOT_FOUND, ex.getCode());
    }

    @Test
    void save_nullTranslations_throwsBusinessException() {
        when(post.translations()).thenReturn(null);

        assertThrows(BusinessException.class, () -> postOutput.save(post, null, null, author));
    }

    @Test
    void save_emptyTranslations_throwsBusinessException() {
        when(post.translations()).thenReturn(Map.of());

        assertThrows(BusinessException.class, () -> postOutput.save(post, List.of(), List.of(), author));
    }

    @Test
    void save_validRequest_savesEntityWithContentLanguage() {
        when(post.translations()).thenReturn(Map.of(Language.ENGLISH, postContentModel));
        when(postContentModel.title()).thenReturn("My Title");
        when(postContentModel.content()).thenReturn("Some content here");
        when(postOutputMapper.toContentEntity(postContentModel)).thenReturn(newContent);
        when(postRepository.countBySlugAndIdNot("my-title", null)).thenReturn(0L);
        when(postRepository.save(any(PostEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        postOutput.save(post, null, null, author);

        verify(newContent).setLanguage(Language.ENGLISH);
        verify(postRepository).save(any(PostEntity.class));
    }

    // Committed behavior: PostOutput.generateUniqueSlug concatenates the counter as a
    // string ("my-title-" + 2 + 1 -> "my-title-21"). Suspected production bug, reported.
    @Test
    void save_slugConflict_appendsCounterToSlug() {
        when(post.translations()).thenReturn(Map.of(Language.ENGLISH, postContentModel));
        when(postContentModel.title()).thenReturn("My Title");
        when(postContentModel.content()).thenReturn("Content");
        when(postOutputMapper.toContentEntity(postContentModel)).thenReturn(newContent);
        when(postRepository.countBySlugAndIdNot("my-title", null)).thenReturn(2L);
        when(postRepository.save(any(PostEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        postOutput.save(post, null, null, author);

        verify(postRepository).save(argThat(e -> "my-title-21".equals(e.getSlug())));
    }

    @Test
    void save_withTagAndProjectIds_resolvesFromRepositories() {
        when(post.translations()).thenReturn(Map.of(Language.ENGLISH, postContentModel));
        when(postContentModel.title()).thenReturn("My Title");
        when(postContentModel.content()).thenReturn("Content");
        when(postOutputMapper.toContentEntity(postContentModel)).thenReturn(newContent);
        when(postRepository.countBySlugAndIdNot(anyString(), isNull())).thenReturn(0L);
        when(postRepository.save(any(PostEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        var tagIds = List.of(UUID.randomUUID());
        var projectIds = List.of(UUID.randomUUID());

        postOutput.save(post, tagIds, projectIds, author);

        verify(tagRepository).findAllById(tagIds);
        verify(projectRepository).findAllById(projectIds);
    }

    @Test
    void update_notFound_throwsNotFoundException() {
        var postId = UUID.randomUUID();
        when(post.translations()).thenReturn(Map.of(Language.ENGLISH, postContentModel));
        when(post.id()).thenReturn(postId);
        when(postRepository.findByIdWithAuthor(postId, author.id(), true)).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class, () -> postOutput.update(post, null, null, author));
        assertEquals(ExceptionCode.POST_NOT_FOUND, ex.getCode());
    }

    @Test
    void update_titleChanged_regeneratesSlug() {
        var postId = UUID.randomUUID();
        when(post.translations()).thenReturn(Map.of(Language.ENGLISH, postContentModel));
        when(post.id()).thenReturn(postId);
        when(postContentModel.title()).thenReturn("Changed Title");
        when(postContentModel.content()).thenReturn("Content");
        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getTitle()).thenReturn("Original Title");
        var contents = new java.util.ArrayList<>(List.of(existingContent));
        when(postEntity.getContents()).thenReturn(contents);
        when(postEntity.getId()).thenReturn(postId);
        when(postRepository.findByIdWithAuthor(postId, author.id(), true)).thenReturn(Optional.of(postEntity));
        when(postRepository.countBySlugAndIdNot("changed-title", postId)).thenReturn(0L);
        when(postRepository.save(postEntity)).thenReturn(postEntity);

        postOutput.update(post, null, null, author);

        verify(postEntity).setSlug("changed-title");
    }

    @Test
    void update_titleUnchanged_keepsSlug() {
        var postId = UUID.randomUUID();
        when(post.translations()).thenReturn(Map.of(Language.ENGLISH, postContentModel));
        when(post.id()).thenReturn(postId);
        when(postContentModel.title()).thenReturn("Same Title");
        when(postContentModel.content()).thenReturn("New Content");
        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getTitle()).thenReturn("Same Title");
        var contents = new java.util.ArrayList<>(List.of(existingContent));
        when(postEntity.getContents()).thenReturn(contents);
        when(postRepository.findByIdWithAuthor(postId, author.id(), true)).thenReturn(Optional.of(postEntity));
        when(postRepository.save(postEntity)).thenReturn(postEntity);

        postOutput.update(post, null, null, author);

        verify(postEntity, never()).setSlug(anyString());
        verify(postRepository, never()).countBySlugAndIdNot(anyString(), any());
        verify(existingContent).setContent("New Content");
    }

    @Test
    void update_newLanguage_addsTranslationAndRemovesOld() {
        var postId = UUID.randomUUID();
        when(post.translations()).thenReturn(Map.of(Language.PORTUGUESE, postContentModel));
        when(post.id()).thenReturn(postId);
        when(postContentModel.title()).thenReturn("Titulo");
        when(postContentModel.content()).thenReturn("Conteudo");
        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getTitle()).thenReturn("Old Title");
        var contents = new java.util.ArrayList<>(List.of(existingContent));
        when(postEntity.getContents()).thenReturn(contents);
        when(postOutputMapper.toContentEntity(postContentModel)).thenReturn(newContent);
        when(newContent.getLanguage()).thenReturn(Language.PORTUGUESE);
        when(postRepository.findByIdWithAuthor(postId, author.id(), true)).thenReturn(Optional.of(postEntity));
        when(postRepository.save(postEntity)).thenReturn(postEntity);

        postOutput.update(post, null, null, author);

        verify(newContent).setLanguage(Language.PORTUGUESE);
        verify(newContent).setPost(postEntity);
        assertEquals(1, contents.size());
        assertSame(newContent, contents.getFirst());
    }

    @Test
    void deleteAll_admin_bypassesOwnershipCheck() {
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        postOutput.deleteAll(ids, author);

        verify(postRepository).deleteByIdWithAuthor(ids, author.id(), true);
    }

    @Test
    void deleteAll_nonAdmin_requiresOwnership() {
        var ids = List.of(UUID.randomUUID());
        when(author.id()).thenReturn(UUID.randomUUID());
        when(author.role()).thenReturn("org:member");

        postOutput.deleteAll(ids, author);

        verify(postRepository).deleteByIdWithAuthor(ids, author.id(), false);
    }

    @Test
    void search_propagatesLanguageAndShowDraftsToRepository() {
        when(postQueryModel.query()).thenReturn("spring");
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);
        when(postRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString(), anyString()))
                .thenReturn(new PageImpl<>(List.of()));

        postOutput.search(input(postQueryModel, "createdAt", Sort.Direction.DESC), null);

        verify(postRepository).search(eq("spring"), isNull(), isNull(), eq("ENGLISH"), eq(false),
                any(PageRequest.class), eq("createdAt"), eq("DESC"));
    }

    @Test
    void search_nullLanguage_passesNullToRepository() {
        when(postQueryModel.language()).thenReturn(null);
        when(postRepository.search(any(), any(), any(), isNull(), anyBoolean(), any(PageRequest.class), anyString(), anyString()))
                .thenReturn(new PageImpl<>(List.of()));

        postOutput.search(input(postQueryModel, "createdAt", Sort.Direction.DESC), null);

        verify(postRepository).search(any(), any(), any(), isNull(), eq(false),
                any(PageRequest.class), eq("createdAt"), eq("DESC"));
    }

    @Test
    void search_withAuthor_showsDrafts() {
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);
        when(postRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString(), anyString()))
                .thenReturn(new PageImpl<>(List.of()));

        postOutput.search(input(postQueryModel, "createdAt", Sort.Direction.DESC), author);

        verify(postRepository).search(any(), any(), any(), any(), eq(true),
                any(PageRequest.class), eq("createdAt"), eq("DESC"));
    }

    @Test
    void search_sortAndDirection_passThroughUnmapped() {
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);
        when(postRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString(), anyString()))
                .thenReturn(new PageImpl<>(List.of()));

        postOutput.search(input(postQueryModel, "viewCount", Sort.Direction.ASC), null);

        verify(postRepository).search(any(), any(), any(), any(), eq(false),
                any(PageRequest.class), eq("viewCount"), eq("ASC"));
    }

    @Test
    void search_emptyPage_returnsEmptyContent() {
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);
        when(postRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString(), anyString()))
                .thenReturn(new PageImpl<>(List.of()));

        var result = postOutput.search(input(postQueryModel, "createdAt", Sort.Direction.DESC), null);

        assertTrue(result.content().isEmpty());
        verify(postRepository, never()).findTagIds(any());
        verifyNoInteractions(postOutputMapper);
    }

    @Test
    void search_mapsPageWithEmptyProjectIdsAndTagIdsFromRepository() {
        var entityId = UUID.randomUUID();
        var tagId = UUID.randomUUID();
        when(postEntity.getId()).thenReturn(entityId);
        when(postRepository.findTagIds(entityId)).thenReturn(List.of(tagId));
        when(postOutputMapper.toModel(postEntity, List.of(), List.of(tagId))).thenReturn(expectedResult);
        when(expectedResult.translations()).thenReturn(Map.of());
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);
        when(postRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString(), anyString()))
                .thenReturn(new PageImpl<>(List.of(postEntity)));

        var result = postOutput.search(input(postQueryModel, "createdAt", Sort.Direction.DESC), null);

        assertEquals(List.of(expectedResult), result.content());
    }

    @Test
    void search_preservesPageOrder() {
        var idA = UUID.randomUUID();
        var idB = UUID.randomUUID();
        when(postEntity.getId()).thenReturn(idA);
        when(secondPostEntity.getId()).thenReturn(idB);
        when(postRepository.findTagIds(idA)).thenReturn(List.of());
        when(postRepository.findTagIds(idB)).thenReturn(List.of());
        when(postOutputMapper.toModel(postEntity, List.of(), List.of())).thenReturn(expectedResult);
        when(postOutputMapper.toModel(secondPostEntity, List.of(), List.of())).thenReturn(secondResult);
        when(expectedResult.translations()).thenReturn(Map.of());
        when(secondResult.translations()).thenReturn(Map.of());
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);
        when(postRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString(), anyString()))
                .thenReturn(new PageImpl<>(List.of(postEntity, secondPostEntity)));

        var result = postOutput.search(input(postQueryModel, "createdAt", Sort.Direction.DESC), null);

        assertEquals(List.of(expectedResult, secondResult), result.content());
    }

    // Committed behavior: search filters non-requested languages in Java (removeIf).
    @Test
    void search_filtersOtherLanguagesInJava() {
        var entityId = UUID.randomUUID();
        var translations = new HashMap<Language, PostContentModel>();
        translations.put(Language.ENGLISH, null);
        translations.put(Language.PORTUGUESE, null);
        when(postEntity.getId()).thenReturn(entityId);
        when(postRepository.findTagIds(entityId)).thenReturn(List.of());
        when(postOutputMapper.toModel(eq(postEntity), anyList(), anyList())).thenReturn(expectedResult);
        when(expectedResult.translations()).thenReturn(translations);
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);
        when(postRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString(), anyString()))
                .thenReturn(new PageImpl<>(List.of(postEntity)));

        var result = postOutput.search(input(postQueryModel, "createdAt", Sort.Direction.DESC), null);

        assertEquals(1, result.content().size());
        assertTrue(translations.containsKey(Language.ENGLISH));
        assertFalse(translations.containsKey(Language.PORTUGUESE));
    }

    @Test
    void search_isTransactionalReadOnly() throws NoSuchMethodException {
        var method = PostOutput.class.getMethod("search", PaginatedInput.class, AuthorModel.class);

        var transactional = method.getAnnotation(Transactional.class);

        assertNotNull(transactional);
        assertTrue(transactional.readOnly());
    }

    private PaginatedInput<PostQueryModel> input(PostQueryModel query, String sort, Sort.Direction direction) {
        return new PaginatedInput<>(query, 0, 10, sort, direction);
    }
}
