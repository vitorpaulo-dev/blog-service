package dev.vitorpaulo.blog.output.post;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.domain.PostContentEntity;
import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.output.mapper.PostOutputMapper;
import dev.vitorpaulo.blog.output.mapper.ProjectMapper;
import dev.vitorpaulo.blog.output.mapper.TagMapper;
import dev.vitorpaulo.blog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

    @InjectMocks
    private PostOutput postOutput;

    private AuthorModel author;

    @BeforeEach
    void setUp() {
        author = new AuthorModel(UUID.randomUUID(), "clerk-1", "Test Author", "test-author", null, "org:admin", null);
    }

    @Nested
    class FindById {

        @Test
        void shouldReturnPostWhenFound() {
            var entityId = UUID.randomUUID();
            var entity = new PostEntity();
            var expectedModel = mock(PostModel.class);

            when(postRepository.findById(entityId)).thenReturn(Optional.of(entity));
            when(postOutputMapper.toModel(entity)).thenReturn(expectedModel);

            var result = postOutput.findById(entityId);

            assertEquals(expectedModel, result);
        }

        @Test
        void shouldThrowNotFoundWhenPostDoesNotExist() {
            when(postRepository.findById(any())).thenReturn(Optional.empty());

            var ex = assertThrows(NotFoundException.class, () -> postOutput.findById(UUID.randomUUID()));
            assertEquals(ExceptionCode.POST_NOT_FOUND, ex.getCode());
        }
    }

    @Nested
    class FindBySlugAndIncrementView {

        @Test
        void shouldIncrementViewCountAndReturnFilteredModel() {
            var entity = new PostEntity();
            entity.setViewCount(5L);
            var savedEntity = new PostEntity();
            var expectedModel = mock(PostModel.class);

            when(postRepository.findBySlugAndLanguage("my-post", Language.ENGLISH)).thenReturn(Optional.of(entity));
            when(postRepository.save(entity)).thenReturn(savedEntity);
            when(postOutputMapper.toModel(savedEntity, Language.ENGLISH)).thenReturn(expectedModel);

            var result = postOutput.findBySlugAndIncrementView("my-post", Language.ENGLISH);

            assertEquals(6L, entity.getViewCount());
            assertEquals(expectedModel, result);
            verify(postRepository).save(entity);
        }

        @Test
        void shouldSetViewCountToOneWhenNull() {
            var entity = new PostEntity();
            entity.setViewCount(null);

            when(postRepository.findBySlugAndLanguage("my-post", Language.ENGLISH)).thenReturn(Optional.of(entity));
            when(postRepository.save(entity)).thenReturn(entity);
            when(postOutputMapper.toModel(eq(entity), eq(Language.ENGLISH))).thenReturn(mock(PostModel.class));

            postOutput.findBySlugAndIncrementView("my-post", Language.ENGLISH);

            assertEquals(1L, entity.getViewCount());
        }

        @Test
        void shouldThrowNotFoundWhenSlugDoesNotExist() {
            when(postRepository.findBySlugAndLanguage(anyString(), any())).thenReturn(Optional.empty());

            var ex = assertThrows(NotFoundException.class,
                    () -> postOutput.findBySlugAndIncrementView("nonexistent", Language.ENGLISH));
            assertEquals(ExceptionCode.POST_SLUG_NOT_FOUND, ex.getCode());
        }
    }

    @Nested
    class Save {

        @Test
        void shouldThrowWhenTranslationsAreEmpty() {
            var post = mock(PostModel.class);
            when(post.translations()).thenReturn(Map.of());

            assertThrows(BusinessException.class,
                    () -> postOutput.save(post, List.of(), List.of(), author));
        }

        @Test
        void shouldThrowWhenTranslationsAreNull() {
            var post = mock(PostModel.class);
            when(post.translations()).thenReturn(null);

            assertThrows(BusinessException.class,
                    () -> postOutput.save(post, List.of(), List.of(), author));
        }

        @Test
        void shouldSavePostWithEnglishTranslation() {
            var translations = Map.of(Language.ENGLISH, new PostContentModel("My Title", "Some content here"));
            var post = mock(PostModel.class);
            when(post.translations()).thenReturn(translations);

            var contentEntity = new PostContentEntity();
            when(postOutputMapper.toContentEntity(any())).thenReturn(contentEntity);
            when(postOutputMapper.toAuthorEntity(any())).thenReturn(new dev.vitorpaulo.blog.domain.AuthorEntity());

            when(postRepository.countBySlugAndIdNot(anyString(), isNull())).thenReturn(0L);
            when(postOutputMapper.toModel(any(PostEntity.class))).thenReturn(mock(PostModel.class));
            when(postRepository.save(any(PostEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            postOutput.save(post, null, null, author);

            verify(postRepository).save(argThat(entity -> {
                assertEquals(1, entity.getContents().size());
                assertEquals(Language.ENGLISH, entity.getContents().get(0).getLanguage());
                return true;
            }));
        }

        @Test
        void shouldGenerateUniqueSlugWhenConflict() {
            var translations = Map.of(Language.ENGLISH, new PostContentModel("My Title", "Content"));
            var post = mock(PostModel.class);
            when(post.translations()).thenReturn(translations);

            var contentEntity = new PostContentEntity();
            when(postOutputMapper.toContentEntity(any())).thenReturn(contentEntity);
            when(postOutputMapper.toAuthorEntity(any())).thenReturn(new dev.vitorpaulo.blog.domain.AuthorEntity());

            when(postRepository.countBySlugAndIdNot("my-title", null)).thenReturn(2L);
            when(postOutputMapper.toModel(any(PostEntity.class))).thenReturn(mock(PostModel.class));
            when(postRepository.save(any(PostEntity.class))).thenAnswer(inv -> inv.getArgument(0));

            postOutput.save(post, null, null, author);

            verify(postRepository).save(argThat(e -> e.getSlug().equals("my-title-21")));
        }
    }

    @Nested
    class Update {

        @Test
        void shouldThrowWhenTranslationsAreEmpty() {
            var post = mock(PostModel.class);
            when(post.translations()).thenReturn(Map.of());

            assertThrows(BusinessException.class,
                    () -> postOutput.update(post, List.of(), List.of(), author));
        }

        @Test
        void shouldThrowWhenPostNotFound() {
            var postId = UUID.randomUUID();
            var translations = Map.of(Language.ENGLISH, new PostContentModel("Title", "Content"));
            var post = mock(PostModel.class);
            when(post.translations()).thenReturn(translations);
            when(post.id()).thenReturn(postId);

            when(postRepository.findByIdWithAuthor(postId, author.id(), true)).thenReturn(Optional.empty());

            var ex = assertThrows(NotFoundException.class,
                    () -> postOutput.update(post, List.of(), List.of(), author));
            assertEquals(ExceptionCode.POST_NOT_FOUND, ex.getCode());
        }

        @Test
        void shouldUpdateExistingContentInPlace() {
            var postId = UUID.randomUUID();
            var translations = Map.of(Language.ENGLISH, new PostContentModel("New Title", "New Content"));
            var post = mock(PostModel.class);
            when(post.translations()).thenReturn(translations);
            when(post.id()).thenReturn(postId);

            var existingContent = new PostContentEntity();
            existingContent.setLanguage(Language.ENGLISH);
            existingContent.setTitle("Old Title");
            existingContent.setContent("Old Content");

            var entity = new PostEntity();
            entity.setId(postId);
            entity.setContents(new ArrayList<>(List.of(existingContent)));

            when(postRepository.findByIdWithAuthor(postId, author.id(), true)).thenReturn(Optional.of(entity));
            when(postRepository.save(entity)).thenReturn(entity);
            when(postOutputMapper.toModel(entity)).thenReturn(mock(PostModel.class));

            postOutput.update(post, null, null, author);

            assertEquals("New Title", existingContent.getTitle());
            assertEquals("New Content", existingContent.getContent());
            assertEquals(1, entity.getContents().size());
        }

        @Test
        void shouldAddNewLanguageAndRemoveOld() {
            var postId = UUID.randomUUID();
            var translations = Map.of(Language.PORTUGUESE, new PostContentModel("Titulo", "Conteudo"));
            var post = mock(PostModel.class);
            when(post.translations()).thenReturn(translations);
            when(post.id()).thenReturn(postId);

            var existingContent = new PostContentEntity();
            existingContent.setLanguage(Language.ENGLISH);
            existingContent.setTitle("Old Title");
            existingContent.setContent("Old Content");

            var entity = new PostEntity();
            entity.setId(postId);
            entity.setContents(new ArrayList<>(List.of(existingContent)));

            var newContent = new PostContentEntity();
            when(postOutputMapper.toContentEntity(any())).thenReturn(newContent);

            when(postRepository.findByIdWithAuthor(postId, author.id(), true)).thenReturn(Optional.of(entity));
            when(postRepository.save(entity)).thenReturn(entity);
            when(postOutputMapper.toModel(entity)).thenReturn(mock(PostModel.class));

            postOutput.update(post, null, null, author);

            assertEquals(1, entity.getContents().size());
            assertEquals(Language.PORTUGUESE, entity.getContents().get(0).getLanguage());
        }

        @Test
        void shouldRegenerateSlugWhenTitleChanges() {
            var postId = UUID.randomUUID();
            var translations = Map.of(Language.ENGLISH, new PostContentModel("Changed Title", "Content"));
            var post = mock(PostModel.class);
            when(post.translations()).thenReturn(translations);
            when(post.id()).thenReturn(postId);

            var existingContent = new PostContentEntity();
            existingContent.setLanguage(Language.ENGLISH);
            existingContent.setTitle("Original Title");
            existingContent.setContent("Content");

            var entity = new PostEntity();
            entity.setId(postId);
            entity.setSlug("original-title");
            entity.setContents(new ArrayList<>(List.of(existingContent)));

            when(postRepository.findByIdWithAuthor(postId, author.id(), true)).thenReturn(Optional.of(entity));
            when(postRepository.countBySlugAndIdNot("changed-title", postId)).thenReturn(0L);
            when(postRepository.save(entity)).thenReturn(entity);
            when(postOutputMapper.toModel(entity)).thenReturn(mock(PostModel.class));

            postOutput.update(post, null, null, author);

            assertEquals("changed-title", entity.getSlug());
        }

        @Test
        void shouldNotRegenerateSlugWhenTitleIsSame() {
            var postId = UUID.randomUUID();
            var translations = Map.of(Language.ENGLISH, new PostContentModel("Same Title", "New Content"));
            var post = mock(PostModel.class);
            when(post.translations()).thenReturn(translations);
            when(post.id()).thenReturn(postId);

            var existingContent = new PostContentEntity();
            existingContent.setLanguage(Language.ENGLISH);
            existingContent.setTitle("Same Title");
            existingContent.setContent("Old Content");

            var entity = new PostEntity();
            entity.setId(postId);
            entity.setSlug("same-title");
            entity.setContents(new ArrayList<>(List.of(existingContent)));

            when(postRepository.findByIdWithAuthor(postId, author.id(), true)).thenReturn(Optional.of(entity));
            when(postRepository.save(entity)).thenReturn(entity);
            when(postOutputMapper.toModel(entity)).thenReturn(mock(PostModel.class));

            postOutput.update(post, null, null, author);

            assertEquals("same-title", entity.getSlug());
            verify(postRepository, never()).countBySlugAndIdNot(anyString(), any());
        }
    }

    @Nested
    class DeleteAll {

        @Test
        void shouldDelegateToRepositoryWithAuthorCheck() {
            var ids = List.of(UUID.randomUUID(), UUID.randomUUID());

            postOutput.deleteAll(ids, author);

            verify(postRepository).deleteByIdWithAuthor(ids, author.id(), true);
        }

        @Test
        void shouldPassNonAdminFlag() {
            var nonAdmin = new AuthorModel(UUID.randomUUID(), "clerk-2", "User", "user", null, "org:member", null);
            var ids = List.of(UUID.randomUUID());

            postOutput.deleteAll(ids, nonAdmin);

            verify(postRepository).deleteByIdWithAuthor(ids, nonAdmin.id(), false);
        }
    }

    @Nested
    class Search {

        @Test
        void shouldReturnPaginatedResults() {
            var entity = new PostEntity();
            var model = mock(PostModel.class);
            Page<PostEntity> page = new PageImpl<>(List.of(entity));

            when(postRepository.search(any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                    .thenReturn(page);
            when(postOutputMapper.toModel(entity, Language.ENGLISH)).thenReturn(model);

            var query = new PostQueryModel("test", null, Language.ENGLISH);
            var input = new PaginatedInput<>(query, 0, 10, "createdAt", Sort.Direction.DESC);

            var result = postOutput.search(input, null);

            assertNotNull(result);
            assertEquals(1, result.content().size());
        }

        @Test
        void shouldPassNullLanguageWhenNotSpecified() {
            Page<PostEntity> page = new PageImpl<>(List.of());

            when(postRepository.search(any(), any(), isNull(), anyBoolean(), any(PageRequest.class), anyString()))
                    .thenReturn(page);

            var query = new PostQueryModel(null, null, null);
            var input = new PaginatedInput<>(query, 0, 10, "createdAt", Sort.Direction.DESC);

            postOutput.search(input, null);

            verify(postRepository).search(isNull(), isNull(), isNull(), eq(true), any(PageRequest.class), anyString());
        }

        @Test
        void shouldMapSortPropertyCorrectly() {
            Page<PostEntity> page = new PageImpl<>(List.of());
            when(postRepository.search(any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                    .thenReturn(page);

            var query = new PostQueryModel(null, null, Language.ENGLISH);
            var input = new PaginatedInput<>(query, 0, 10, "viewCount", Sort.Direction.ASC);

            postOutput.search(input, author);

            verify(postRepository).search(any(), any(), any(), anyBoolean(), any(PageRequest.class), eq("view_count ASC"));
        }

        @Test
        void shouldDefaultToCreatedAtSort() {
            Page<PostEntity> page = new PageImpl<>(List.of());
            when(postRepository.search(any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString()))
                    .thenReturn(page);

            var query = new PostQueryModel(null, null, Language.ENGLISH);
            var input = new PaginatedInput<>(query, 0, 10, "unknownField", Sort.Direction.DESC);

            postOutput.search(input, author);

            verify(postRepository).search(any(), any(), any(), anyBoolean(), any(PageRequest.class), eq("created_at DESC, updated_at DESC"));
        }
    }
}
