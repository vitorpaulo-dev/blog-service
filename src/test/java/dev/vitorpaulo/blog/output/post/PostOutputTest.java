package dev.vitorpaulo.blog.output.post;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.domain.AuthorEntity;
import dev.vitorpaulo.blog.domain.PostContentEntity;
import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.output.mapper.PostOutputMapper;
import dev.vitorpaulo.blog.output.mapper.ProjectMapper;
import dev.vitorpaulo.blog.output.mapper.TagMapper;
import dev.vitorpaulo.blog.repository.PostRepository;
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
class PostOutputTest {

    @Mock private PostRepository postRepository;
    @Mock private PostOutputMapper postOutputMapper;
    @Mock private TagMapper tagMapper;
    @Mock private ProjectMapper projectMapper;
    @Mock private PostModel post;
    @Mock private AuthorModel author;
    @Mock private PostEntity postEntity;
    @Mock private PostContentEntity postContentEntity;
    @Mock private PostContentEntity existingContent;
    @Mock private PostContentModel postContentModel;
    @Mock private PostModel expectedResult;
    @Mock private AuthorEntity authorEntity;
    @Mock private PostQueryModel postQueryModel;

    @InjectMocks
    private PostOutput postOutput;

    @BeforeEach
    void setUp() {
        lenient().when(author.id()).thenReturn(UUID.randomUUID());
        lenient().when(author.role()).thenReturn("org:admin");
    }

    @Test
    void findById_found_returnsPost() {
        var entityId = UUID.randomUUID();

        when(postRepository.findById(entityId)).thenReturn(Optional.of(postEntity));
        when(postOutputMapper.toModel(postEntity)).thenReturn(expectedResult);

        var result = postOutput.findById(entityId);

        assertEquals(expectedResult, result);
    }

    @Test
    void findById_notFound_throwsNotFoundException() {
        when(postRepository.findById(any())).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class, () -> postOutput.findById(UUID.randomUUID()));
        assertEquals(ExceptionCode.POST_NOT_FOUND, ex.getCode());
    }

    @Test
    void findBySlugAndIncrementView_found_incrementsViewCount() {
        when(postRepository.findBySlugAndLanguage("my-post", Language.ENGLISH)).thenReturn(Optional.of(postEntity));
        when(postEntity.getViewCount()).thenReturn(5L);
        when(postRepository.save(postEntity)).thenReturn(postEntity);
        when(postOutputMapper.toModel(postEntity, Language.ENGLISH)).thenReturn(expectedResult);

        var result = postOutput.findBySlugAndIncrementView("my-post", Language.ENGLISH);

        verify(postEntity).setViewCount(6L);
        assertEquals(expectedResult, result);
        verify(postRepository).save(postEntity);
    }

    @Test
    void findBySlugAndIncrementView_nullViewCount_setsToOne() {
        when(postRepository.findBySlugAndLanguage("my-post", Language.ENGLISH)).thenReturn(Optional.of(postEntity));
        when(postEntity.getViewCount()).thenReturn(null);
        when(postRepository.save(postEntity)).thenReturn(postEntity);
        when(postOutputMapper.toModel(postEntity, Language.ENGLISH)).thenReturn(expectedResult);

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
    void save_emptyTranslations_throwsBusinessException() {
        when(post.translations()).thenReturn(Map.of());

        assertThrows(BusinessException.class,
                () -> postOutput.save(post, List.of(), List.of(), author));
    }

    @Test
    void save_nullTranslations_throwsBusinessException() {
        when(post.translations()).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> postOutput.save(post, List.of(), List.of(), author));
    }

    @Test
    void save_withEnglishTranslation_savesPost() {
        var translations = Map.of(Language.ENGLISH, postContentModel);
        when(post.translations()).thenReturn(translations);
        when(postContentModel.title()).thenReturn("My Title");
        when(postContentModel.content()).thenReturn("Some content here");

        when(postOutputMapper.toContentEntity(postContentModel)).thenReturn(postContentEntity);
        when(postOutputMapper.toAuthorEntity(author)).thenReturn(authorEntity);

        when(postRepository.countBySlugAndIdNot(anyString(), isNull())).thenReturn(0L);
        when(postOutputMapper.toModel(any(PostEntity.class))).thenReturn(expectedResult);
        when(postRepository.save(any(PostEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        postOutput.save(post, null, null, author);

        verify(postRepository).save(any(PostEntity.class));
        verify(postContentEntity).setLanguage(Language.ENGLISH);
    }

    @Test
    void save_withConflict_generatesUniqueSlug() {
        var translations = Map.of(Language.ENGLISH, postContentModel);
        when(post.translations()).thenReturn(translations);
        when(postContentModel.title()).thenReturn("My Title");
        when(postContentModel.content()).thenReturn("Content");

        when(postOutputMapper.toContentEntity(postContentModel)).thenReturn(postContentEntity);
        when(postOutputMapper.toAuthorEntity(author)).thenReturn(authorEntity);

        when(postRepository.countBySlugAndIdNot("my-title", null)).thenReturn(2L);
        when(postOutputMapper.toModel(any(PostEntity.class))).thenReturn(expectedResult);
        when(postRepository.save(any(PostEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        postOutput.save(post, null, null, author);

        verify(postRepository).save(argThat(e -> e.getSlug().equals("my-title-21")));
    }

    @Test
    void update_emptyTranslations_throwsBusinessException() {
        when(post.translations()).thenReturn(Map.of());

        assertThrows(BusinessException.class,
                () -> postOutput.update(post, List.of(), List.of(), author));
    }

    @Test
    void update_notFound_throwsNotFoundException() {
        var postId = UUID.randomUUID();
        var translations = Map.of(Language.ENGLISH, postContentModel);
        when(post.translations()).thenReturn(translations);
        when(post.id()).thenReturn(postId);

        when(postRepository.findByIdWithAuthor(postId, author.id(), true)).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class,
                () -> postOutput.update(post, List.of(), List.of(), author));
        assertEquals(ExceptionCode.POST_NOT_FOUND, ex.getCode());
    }

    @Test
    void update_existingContent_updatesInPlace() {
        var postId = UUID.randomUUID();
        var translations = Map.of(Language.ENGLISH, postContentModel);
        when(post.translations()).thenReturn(translations);
        when(post.id()).thenReturn(postId);
        when(postContentModel.title()).thenReturn("New Title");
        when(postContentModel.content()).thenReturn("New Content");

        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getTitle()).thenReturn("Old Title");

        var contents = new ArrayList<>(List.of(existingContent));
        when(postEntity.getContents()).thenReturn(contents);

        when(postRepository.findByIdWithAuthor(postId, author.id(), true)).thenReturn(Optional.of(postEntity));
        when(postRepository.save(postEntity)).thenReturn(postEntity);
        when(postOutputMapper.toModel(postEntity)).thenReturn(expectedResult);

        postOutput.update(post, null, null, author);

        verify(existingContent).setTitle("New Title");
        verify(existingContent).setContent("New Content");
        assertEquals(1, postEntity.getContents().size());
    }

    @Test
    void update_newLanguage_addsAndRemovesOld() {
        var postId = UUID.randomUUID();
        var translations = Map.of(Language.PORTUGUESE, postContentModel);
        when(post.translations()).thenReturn(translations);
        when(post.id()).thenReturn(postId);
        when(postContentModel.title()).thenReturn("Titulo");
        when(postContentModel.content()).thenReturn("Conteudo");

        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getTitle()).thenReturn("Old Title");

        var contents = new ArrayList<>(List.of(existingContent));
        when(postEntity.getContents()).thenReturn(contents);

        when(postOutputMapper.toContentEntity(postContentModel)).thenReturn(postContentEntity);
        when(postContentEntity.getLanguage()).thenReturn(Language.PORTUGUESE);

        when(postRepository.findByIdWithAuthor(postId, author.id(), true)).thenReturn(Optional.of(postEntity));
        when(postRepository.save(postEntity)).thenReturn(postEntity);
        when(postOutputMapper.toModel(postEntity)).thenReturn(expectedResult);

        postOutput.update(post, null, null, author);

        verify(postContentEntity).setLanguage(Language.PORTUGUESE);
        verify(postContentEntity).setPost(postEntity);
        assertEquals(1, contents.size());
    }

    @Test
    void update_titleChanged_regeneratesSlug() {
        var postId = UUID.randomUUID();
        var translations = Map.of(Language.ENGLISH, postContentModel);
        when(post.translations()).thenReturn(translations);
        when(post.id()).thenReturn(postId);
        when(postContentModel.title()).thenReturn("Changed Title");
        when(postContentModel.content()).thenReturn("Content");

        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getTitle()).thenReturn("Original Title");

        var contents = new ArrayList<>(List.of(existingContent));
        when(postEntity.getId()).thenReturn(postId);
        when(postEntity.getContents()).thenReturn(contents);

        when(postRepository.findByIdWithAuthor(postId, author.id(), true)).thenReturn(Optional.of(postEntity));
        when(postRepository.countBySlugAndIdNot("changed-title", postId)).thenReturn(0L);
        when(postRepository.save(postEntity)).thenReturn(postEntity);
        when(postOutputMapper.toModel(postEntity)).thenReturn(expectedResult);

        postOutput.update(post, null, null, author);

        verify(postEntity).setSlug("changed-title");
    }

    @Test
    void update_titleSame_keepsSlug() {
        var postId = UUID.randomUUID();
        var translations = Map.of(Language.ENGLISH, postContentModel);
        when(post.translations()).thenReturn(translations);
        when(post.id()).thenReturn(postId);
        when(postContentModel.title()).thenReturn("Same Title");
        when(postContentModel.content()).thenReturn("New Content");

        when(existingContent.getLanguage()).thenReturn(Language.ENGLISH);
        when(existingContent.getTitle()).thenReturn("Same Title");

        var contents = new ArrayList<>(List.of(existingContent));
        when(postEntity.getContents()).thenReturn(contents);

        when(postRepository.findByIdWithAuthor(postId, author.id(), true)).thenReturn(Optional.of(postEntity));
        when(postRepository.save(postEntity)).thenReturn(postEntity);
        when(postOutputMapper.toModel(postEntity)).thenReturn(expectedResult);

        postOutput.update(post, null, null, author);

        verify(postEntity, never()).setSlug(anyString());
        verify(postRepository, never()).countBySlugAndIdNot(anyString(), any());
        verify(existingContent).setContent("New Content");
    }

    @Test
    void deleteAll_withAuthor_delegatesToRepository() {
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        postOutput.deleteAll(ids, author);

        verify(postRepository).deleteByIdWithAuthor(ids, author.id(), true);
    }

    @Test
    void deleteAll_nonAdmin_passesFalseFlag() {
        var ids = List.of(UUID.randomUUID());
        when(author.id()).thenReturn(UUID.randomUUID());
        when(author.role()).thenReturn("org:member");

        postOutput.deleteAll(ids, author);

        verify(postRepository).deleteByIdWithAuthor(ids, author.id(), false);
    }

    @Test
    void search_withLanguage_returnsPaginatedResults() {
        Page<PostEntity> page = new PageImpl<>(List.of(postEntity));

        when(postRepository.search(any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);
        when(postOutputMapper.toModel(postEntity, Language.ENGLISH)).thenReturn(expectedResult);
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);

        var input = new PaginatedInput<>(postQueryModel, 0, 10, "createdAt", Sort.Direction.DESC);

        var result = postOutput.search(input, null);

        assertNotNull(result);
        assertEquals(1, result.content().size());
    }

    @Test
    void search_nullLanguage_passesNullToRepository() {
        Page<PostEntity> page = new PageImpl<>(List.of());

        when(postRepository.search(any(), any(), isNull(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);
        when(postQueryModel.language()).thenReturn(null);

        var input = new PaginatedInput<>(postQueryModel, 0, 10, "createdAt", Sort.Direction.DESC);

        postOutput.search(input, null);

        verify(postRepository).search(isNull(), isNull(), isNull(), eq(true), any(PageRequest.class), anyString());
    }

    @Test
    void search_viewCountSort_mapsCorrectly() {
        Page<PostEntity> page = new PageImpl<>(List.of());
        when(postRepository.search(any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);

        var input = new PaginatedInput<>(postQueryModel, 0, 10, "viewCount", Sort.Direction.ASC);

        postOutput.search(input, author);

        verify(postRepository).search(any(), any(), any(), anyBoolean(), any(PageRequest.class), eq("view_count ASC"));
    }

    @Test
    void search_unknownField_defaultsToCreatedAtSort() {
        Page<PostEntity> page = new PageImpl<>(List.of());
        when(postRepository.search(any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                .thenReturn(page);
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);

        var input = new PaginatedInput<>(postQueryModel, 0, 10, "unknownField", Sort.Direction.DESC);

        postOutput.search(input, author);

        verify(postRepository).search(any(), any(), any(), anyBoolean(), any(PageRequest.class), eq("created_at DESC, updated_at DESC"));
    }
}
