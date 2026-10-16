package dev.vitorpaulo.blog.output.post;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.config.cache.CacheConfig;
import dev.vitorpaulo.blog.config.security.AuthorRoleChecker;
import dev.vitorpaulo.blog.domain.PostContentEntity;
import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.audio.AudioModel;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.output.mapper.PostOutputMapper;
import dev.vitorpaulo.blog.output.audio.AudioOutput;
import dev.vitorpaulo.blog.repository.AuthorRepository;
import dev.vitorpaulo.blog.repository.PostRepository;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import dev.vitorpaulo.blog.repository.RedisRepository;
import dev.vitorpaulo.blog.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    @Mock private RedisRepository redisRepository;
    @Mock private AudioOutput audioOutput;
    @Mock private AuthorRoleChecker authorRoleChecker;
    @Mock private PostModel post;
    @Mock private PostModel expectedResult;
    @Mock private PostModel secondResult;
    @Mock private PostEntity postEntity;
    @Mock private PostEntity secondPostEntity;
    @Mock private AudioModel audioModel;
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
        lenient().when(authorRoleChecker.isAdmin()).thenReturn(false);
    }

    @Test
    void findById_found_mapsWithProjectAndTagIds() {
        var id = UUID.randomUUID();
        when(postRepository.findByIdWithContents(id)).thenReturn(Optional.of(postEntity));
        when(postOutputMapper.toModel(eq(postEntity), anyList(), anyList(), anyMap())).thenReturn(expectedResult);

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
    void findBySlug_found_mapsWithProjectTagIdsAudioAndAuthFlag() {
        var postId = UUID.randomUUID();
        when(authorRoleChecker.isAuthenticated()).thenReturn(true);
        when(postRepository.findBySlugAndLanguage("my-post", Language.ENGLISH, true)).thenReturn(Optional.of(postEntity));
        when(postEntity.getId()).thenReturn(postId);
        when(postRepository.findProjectIds(postId)).thenReturn(List.of());
        when(postRepository.findTagIds(postId)).thenReturn(List.of());
        when(audioOutput.artifactMap(postId, Language.ENGLISH)).thenReturn(Map.of());
        when(postOutputMapper.toModel(postEntity, List.of(), List.of(), Map.of())).thenReturn(expectedResult);

        var result = postOutput.findBySlug("my-post", Language.ENGLISH);

        assertEquals(expectedResult, result);
        verify(postRepository).findBySlugAndLanguage("my-post", Language.ENGLISH, true);
    }

    @Test
    void findBySlug_notFound_throwsNotFoundException() {
        when(authorRoleChecker.isAuthenticated()).thenReturn(false);
        when(postRepository.findBySlugAndLanguage("nonexistent", Language.ENGLISH, false)).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class,
                () -> postOutput.findBySlug("nonexistent", Language.ENGLISH));

        assertEquals(ExceptionCode.POST_SLUG_NOT_FOUND, ex.getCode());
        verifyNoInteractions(postOutputMapper);
        verifyNoInteractions(audioOutput);
    }

    @Test
    void findBySlug_audioEnumMap_mappedToSerializationSafeLinkedHashMaps() {
        var postId = UUID.randomUUID();
        var audio = new EnumMap<AudioType, Map<Language, AudioModel>>(AudioType.class);
        var byLanguage = new EnumMap<Language, AudioModel>(Language.class);
        byLanguage.put(Language.PORTUGUESE, audioModel);
        audio.put(AudioType.PODCAST, byLanguage);

        when(authorRoleChecker.isAuthenticated()).thenReturn(false);
        when(postRepository.findBySlugAndLanguage("my-post", Language.PORTUGUESE, false)).thenReturn(Optional.of(postEntity));
        when(postEntity.getId()).thenReturn(postId);
        when(postRepository.findProjectIds(postId)).thenReturn(List.of());
        when(postRepository.findTagIds(postId)).thenReturn(List.of());
        when(audioOutput.artifactMap(postId, Language.PORTUGUESE)).thenReturn(audio);
        when(postOutputMapper.toModel(eq(postEntity), anyList(), anyList(), anyMap())).thenReturn(expectedResult);

        var result = postOutput.findBySlug("my-post", Language.PORTUGUESE);

        assertEquals(expectedResult, result);
        var captor = ArgumentCaptor.forClass(Map.class);
        verify(postOutputMapper).toModel(eq(postEntity), anyList(), anyList(), captor.capture());
        var cachedAudio = captor.getValue();
        assertInstanceOf(LinkedHashMap.class, cachedAudio);
        assertEquals(audio, cachedAudio);
        assertInstanceOf(LinkedHashMap.class, cachedAudio.get(AudioType.PODCAST));
    }

    @ParameterizedTest
    @EnumSource(ReactionType.class)
    void react_keyAbsent_incrementsCountSavesAndSetsKey(ReactionType reactionType) {
        var postId = UUID.randomUUID();
        stubReactionCount(reactionType, 1L);
        when(postRepository.findBySlugAndStatus("my-post", PostStatus.PUBLISHED)).thenReturn(Optional.of(postEntity));
        when(postEntity.getId()).thenReturn(postId);
        when(redisRepository.keyExists(anyString())).thenReturn(false);
        when(postRepository.save(postEntity)).thenReturn(postEntity);
        when(postOutputMapper.toReactionModel(postEntity)).thenReturn(new ReactionModel(1L, 1L, 1L, 1L, 4L));

        var result = postOutput.react("my-post", reactionType, "203.0.113.7");

        assertEquals(4L, result.reactionCount());
        assertReactionIncremented(reactionType, postEntity);
        verify(redisRepository).set("post:" + postId + ":reaction:203.0.113.7:" + reactionType, Duration.ofSeconds(604800));
        verify(redisRepository).addToSortedSet(eq("post:" + postId + ":reactions"), startsWith("203.0.113.7:" + reactionType + ":"), anyDouble(), eq(Duration.ofSeconds(604800)));
    }

    @ParameterizedTest
    @EnumSource(ReactionType.class)
    void react_keyAlreadyPresent_returnsCountsUnchanged(ReactionType reactionType) {
        var postId = UUID.randomUUID();
        var expected = new ReactionModel(1L, 2L, 3L, 4L, 10L);
        when(postRepository.findBySlugAndStatus("my-post", PostStatus.PUBLISHED)).thenReturn(Optional.of(postEntity));
        when(postEntity.getId()).thenReturn(postId);
        when(redisRepository.keyExists("post:" + postId + ":reaction:203.0.113.7:" + reactionType)).thenReturn(true);
        when(postOutputMapper.toReactionModel(postEntity)).thenReturn(expected);

        var result = postOutput.react("my-post", reactionType, "203.0.113.7");

        assertEquals(expected, result);
        verify(postRepository, never()).save(any());
        verify(redisRepository, never()).set(anyString(), any());
        verify(redisRepository, never()).addToSortedSet(anyString(), anyString(), anyDouble(), any());
    }

    @Test
    void react_unknownSlug_throwsNotFoundException() {
        when(postRepository.findBySlugAndStatus("nonexistent", PostStatus.PUBLISHED)).thenReturn(Optional.empty());

        var ex = assertThrows(NotFoundException.class,
                () -> postOutput.react("nonexistent", ReactionType.LOVE, "203.0.113.7"));
        assertEquals(ExceptionCode.POST_NOT_FOUND, ex.getCode());
        verifyNoInteractions(redisRepository);
    }

    private void stubReactionCount(ReactionType reactionType, long current) {
        switch (reactionType) {
            case LOVE -> when(postEntity.getLoveCount()).thenReturn(current);
            case CELEBRATE -> when(postEntity.getCelebrateCount()).thenReturn(current);
            case GENIUS -> when(postEntity.getGeniusCount()).thenReturn(current);
            case HELP -> when(postEntity.getHelpCount()).thenReturn(current);
        }
    }

    private void assertReactionIncremented(ReactionType reactionType, PostEntity entity) {
        switch (reactionType) {
            case LOVE -> verify(entity).setLoveCount(2L);
            case CELEBRATE -> verify(entity).setCelebrateCount(2L);
            case GENIUS -> verify(entity).setGeniusCount(2L);
            case HELP -> verify(entity).setHelpCount(2L);
        }
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

        var saved = ArgumentCaptor.forClass(PostEntity.class);
        verify(audioOutput).dispatch(saved.capture());
        assertEquals(1, saved.getValue().getContents().size());
        verify(newContent).setLanguage(Language.ENGLISH);
        verify(postRepository).save(any(PostEntity.class));
    }

    @Test
    void save_slugConflict_appendsCounterToSlug() {
        when(post.translations()).thenReturn(Map.of(Language.ENGLISH, postContentModel));
        when(postContentModel.title()).thenReturn("My Title");
        when(postContentModel.content()).thenReturn("Content");
        when(postOutputMapper.toContentEntity(postContentModel)).thenReturn(newContent);
        when(postRepository.countBySlugAndIdNot("my-title", null)).thenReturn(2L);
        when(postRepository.save(any(PostEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        postOutput.save(post, null, null, author);

        verify(postRepository).save(argThat(e -> "my-title-3".equals(e.getSlug())));
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
        when(authorRoleChecker.isAdmin()).thenReturn(true);
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
        when(authorRoleChecker.isAdmin()).thenReturn(true);
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
        when(authorRoleChecker.isAdmin()).thenReturn(true);
        when(postRepository.findByIdWithAuthor(postId, author.id(), true)).thenReturn(Optional.of(postEntity));
        when(postRepository.save(postEntity)).thenReturn(postEntity);

        postOutput.update(post, null, null, author);

        verify(postEntity, never()).setSlug(anyString());
        verify(postRepository, never()).countBySlugAndIdNot(anyString(), any());
        verify(existingContent).setContent("New Content");
        verify(audioOutput).dispatch(postEntity);
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
        when(authorRoleChecker.isAdmin()).thenReturn(true);
        when(postRepository.findByIdWithAuthor(postId, author.id(), true)).thenReturn(Optional.of(postEntity));
        when(postRepository.save(postEntity)).thenReturn(postEntity);

        postOutput.update(post, null, null, author);

        verify(newContent).setLanguage(Language.PORTUGUESE);
        verify(newContent).setPost(postEntity);
        assertEquals(1, contents.size());
        assertSame(newContent, contents.getFirst());
    }

    @Test
    void findOwnedIds_adminBypass_returnsAllResolvedIds() {
        var firstId = UUID.randomUUID();
        var secondId = UUID.randomUUID();
        var ids = List.of(firstId, secondId);
        when(authorRoleChecker.isAdmin()).thenReturn(true);
        when(postRepository.findAllByIdWithAuthor(ids, author.id(), true))
                .thenReturn(List.of(postEntity, secondPostEntity));
        when(postEntity.getId()).thenReturn(firstId);
        when(secondPostEntity.getId()).thenReturn(secondId);

        var result = postOutput.findOwnedIds(ids, author);

        assertEquals(List.of(firstId, secondId), result);
    }

    @Test
    void findOwnedIds_nonMemberForeignIds_returnsEmptyList() {
        var ids = List.of(UUID.randomUUID());
        when(postRepository.findAllByIdWithAuthor(ids, author.id(), false)).thenReturn(List.of());

        assertTrue(postOutput.findOwnedIds(ids, author).isEmpty());
    }

    @Test
    void deleteByIds_loadsOwnedEntitiesAndDeletesThem() {
        var ids = List.of(UUID.randomUUID());
        when(postRepository.findAllById(ids)).thenReturn(List.of(postEntity));

        postOutput.deleteByIds(ids);

        verify(postRepository).deleteAll(List.of(postEntity));
    }

    @Test
    void deleteByIds_missingIds_deletesResolvableEntitiesOnly() {
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(postRepository.findAllById(ids)).thenReturn(List.of(postEntity));

        postOutput.deleteByIds(ids);

        verify(postRepository).deleteAll(List.of(postEntity));
    }

    @Test
    void search_propagatesLanguageAndShowDraftsToRepository() {
        when(postQueryModel.query()).thenReturn("spring");
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);
        when(postRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString(), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of()));

        postOutput.search(input(postQueryModel, "createdAt", Sort.Direction.DESC), null);

        verify(postRepository).search(eq("spring"), isNull(), isNull(), eq("ENGLISH"), eq(false),
                any(PageRequest.class), eq("createdAt"), eq("DESC"), eq(false));
    }

    @Test
    void search_nullLanguage_passesNullToRepository() {
        when(postQueryModel.language()).thenReturn(null);
        when(postRepository.search(any(), any(), any(), isNull(), anyBoolean(), any(PageRequest.class), anyString(), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of()));

        postOutput.search(input(postQueryModel, "createdAt", Sort.Direction.DESC), null);

        verify(postRepository).search(any(), any(), any(), isNull(), eq(false),
                any(PageRequest.class), eq("createdAt"), eq("DESC"), eq(false));
    }

    @Test
    void search_withAuthor_showsOwnDraftsOnly() {
        var authorId = UUID.randomUUID();
        when(author.id()).thenReturn(authorId);
        when(postQueryModel.authorId()).thenReturn(authorId);
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);
        when(postRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString(), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of()));

        postOutput.search(input(postQueryModel, "createdAt", Sort.Direction.DESC), author);

        verify(postRepository).search(any(), any(), any(), any(), eq(true),
                any(PageRequest.class), eq("createdAt"), eq("DESC"), eq(false));
    }

    @Test
    void search_memberWithOtherAuthorId_hidesDrafts() {
        var filterAuthorId = UUID.randomUUID();
        when(postQueryModel.authorId()).thenReturn(filterAuthorId);
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);
        when(postRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString(), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of()));

        postOutput.search(input(postQueryModel, "createdAt", Sort.Direction.DESC), author);

        verify(postRepository).search(any(), eq(filterAuthorId), any(), any(), eq(false),
                any(PageRequest.class), eq("createdAt"), eq("DESC"), eq(false));
    }

    @Test
    void search_sortAndDirection_passThroughUnmapped() {
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);
        when(postRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString(), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of()));

        postOutput.search(input(postQueryModel, "viewCount", Sort.Direction.ASC), null);

        verify(postRepository).search(any(), any(), any(), any(), eq(false),
                any(PageRequest.class), eq("viewCount"), eq("ASC"), eq(false));
    }

    @Test
    void search_nullQuery_passesNullFiltersAndAscendingDirection() {
        when(postRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString(), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of()));

        var result = postOutput.search(new PaginatedInput<PostQueryModel>(null, 0, 10, null, null), null);

        assertTrue(result.content().isEmpty());
        verify(postRepository).search(isNull(), isNull(), isNull(), isNull(), eq(false),
                any(PageRequest.class), eq("createdAt"), eq("ASC"), eq(true));
    }

    @Test
    void search_nullDirection_defaultsToAscending() {
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);
        when(postRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString(), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of()));

        postOutput.search(input(postQueryModel, "createdAt", null), null);

        verify(postRepository).search(any(), any(), any(), eq("ENGLISH"), eq(false),
                any(PageRequest.class), eq("createdAt"), eq("ASC"), eq(false));
    }

    @Test
    void search_nullSortWithExplicitDirection_appliesDefaultWithTieBreaker() {
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);
        when(postRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString(), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of()));

        postOutput.search(input(postQueryModel, null, Sort.Direction.DESC), null);

        verify(postRepository).search(any(), any(), any(), eq("ENGLISH"), eq(false),
                any(PageRequest.class), eq("createdAt"), eq("DESC"), eq(true));
    }

    @Test
    void search_emptyPage_returnsEmptyContent() {
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);
        when(postRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString(), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of()));

        var result = postOutput.search(input(postQueryModel, "createdAt", Sort.Direction.DESC), null);

        assertTrue(result.content().isEmpty());
        verify(postRepository, never()).findTagIds(any());
        verifyNoInteractions(postOutputMapper);
    }

    @Test
    void search_preservesPageOrder() {
        var idA = UUID.randomUUID();
        var idB = UUID.randomUUID();
        when(postEntity.getId()).thenReturn(idA);
        when(secondPostEntity.getId()).thenReturn(idB);
        when(postRepository.findTagIds(idA)).thenReturn(List.of());
        when(postRepository.findTagIds(idB)).thenReturn(List.of());
        when(postOutputMapper.toModel(postEntity, List.of(), List.of(), Map.of())).thenReturn(expectedResult);
        when(postOutputMapper.toModel(secondPostEntity, List.of(), List.of(), Map.of())).thenReturn(secondResult);
        when(expectedResult.translations()).thenReturn(Map.of());
        when(secondResult.translations()).thenReturn(Map.of());
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);
        when(postRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString(), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of(postEntity, secondPostEntity)));

        var result = postOutput.search(input(postQueryModel, "createdAt", Sort.Direction.DESC), null);

        assertEquals(List.of(expectedResult, secondResult), result.content());
    }

    @Test
    void search_filtersOtherLanguagesInJava() {
        var entityId = UUID.randomUUID();
        var translations = new HashMap<Language, PostContentModel>();
        translations.put(Language.ENGLISH, null);
        translations.put(Language.PORTUGUESE, null);
        when(postEntity.getId()).thenReturn(entityId);
        when(postRepository.findTagIds(entityId)).thenReturn(List.of());
        when(postOutputMapper.toModel(eq(postEntity), anyList(), anyList(), anyMap())).thenReturn(expectedResult);
        when(expectedResult.translations()).thenReturn(translations);
        when(postQueryModel.language()).thenReturn(Language.ENGLISH);
        when(postRepository.search(any(), any(), any(), any(), anyBoolean(), any(PageRequest.class), anyString(), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of(postEntity)));

        var result = postOutput.search(input(postQueryModel, "createdAt", Sort.Direction.DESC), null);

        assertEquals(1, result.content().size());
        assertTrue(translations.containsKey(Language.ENGLISH));
        assertFalse(translations.containsKey(Language.PORTUGUESE));
    }

    @Test
    void setFeaturedWeights_admin_clearsAndSetsWeights() {
        var firstId = UUID.randomUUID();
        var secondId = UUID.randomUUID();
        when(postRepository.findAllById(List.of(firstId, secondId))).thenReturn(List.of(postEntity, secondPostEntity));

        postOutput.setFeaturedWeights(
            List.of(new FeaturedPostModel(firstId, 1), new FeaturedPostModel(secondId, 2)),
            author
        );

        verify(postRepository).clearFeaturedWeights();
        verify(postRepository).updateWeight(firstId, 1);
        verify(postRepository).updateWeight(secondId, 2);
    }

    @Test
    void setFeaturedWeights_emptyList_clearsAllWithoutUpdates() {
        postOutput.setFeaturedWeights(List.of(), author);

        verify(postRepository).clearFeaturedWeights();
        verify(postRepository, never()).updateWeight(any(), any());
    }

    @Test
    void setFeaturedWeights_missingPost_throwsNotFoundException() {
        var featureId = UUID.randomUUID();
        when(postRepository.findAllById(List.of(featureId))).thenReturn(List.of());

        var ex = assertThrows(NotFoundException.class,
                () -> postOutput.setFeaturedWeights(List.of(new FeaturedPostModel(featureId, 1)), author));
        assertEquals(ExceptionCode.POST_NOT_FOUND, ex.getCode());
        verify(postRepository, never()).clearFeaturedWeights();
    }

    @Test
    void findFeatured_preservesRepositoryOrderAndFiltersToLanguage() {
        var translations = new HashMap<Language, PostContentModel>();
        translations.put(Language.ENGLISH, null);
        translations.put(Language.PORTUGUESE, null);
        when(postRepository.findFeatured()).thenReturn(List.of(postEntity, secondPostEntity));
        when(postOutputMapper.toModel(postEntity, List.of(), List.of(), Map.of())).thenReturn(expectedResult);
        when(postOutputMapper.toModel(secondPostEntity, List.of(), List.of(), Map.of())).thenReturn(secondResult);
        when(expectedResult.translations()).thenReturn(translations);
        when(secondResult.translations()).thenReturn(Map.of());

        var result = postOutput.findFeatured(Language.ENGLISH);

        assertEquals(List.of(expectedResult, secondResult), result);
        assertTrue(translations.containsKey(Language.ENGLISH));
        assertFalse(translations.containsKey(Language.PORTUGUESE));
    }

    @Test
    void findFeatured_returnsMutableListRoot() {
        when(postRepository.findFeatured()).thenReturn(List.of());

        var result = postOutput.findFeatured(Language.ENGLISH);

        assertInstanceOf(ArrayList.class, result);
    }

    @Test
    void findBySlug_isCacheablePostSlug() throws NoSuchMethodException {
        var cacheable = PostOutput.class
            .getDeclaredMethod("findBySlug", String.class, Language.class)
            .getAnnotation(Cacheable.class);

        assertNotNull(cacheable);
        assertEquals(Set.of(CacheConfig.POST_SLUG), Set.of(cacheable.cacheNames()));
        assertEquals("@cacheKeys.slug(#slug, #language, @authorRoleChecker.isAuthenticated())", cacheable.key());
    }

    @Test
    void findFeatured_isCacheableFeaturedPosts() throws NoSuchMethodException {
        var cacheable = PostOutput.class
            .getDeclaredMethod("findFeatured", Language.class)
            .getAnnotation(Cacheable.class);

        assertNotNull(cacheable);
        assertEquals(Set.of(CacheConfig.FEATURED_POSTS), Set.of(cacheable.cacheNames()));
        assertEquals("@cacheKeys.featured(#language)", cacheable.key());
    }

    @Test
    void search_isCacheablePostSearch() throws NoSuchMethodException {
        var cacheable = PostOutput.class
            .getDeclaredMethod("search", PaginatedInput.class, AuthorModel.class)
            .getAnnotation(Cacheable.class);

        assertNotNull(cacheable);
        assertEquals(Set.of(CacheConfig.POST_SEARCH), Set.of(cacheable.cacheNames()));
        assertEquals("@cacheKeys.postSearch(#pageableInput, #author)", cacheable.key());
    }

    @Test
    void save_evictsPostCachesAllEntries() throws NoSuchMethodException {
        var evict = PostOutput.class
            .getDeclaredMethod("save", PostModel.class, List.class, List.class, AuthorModel.class)
            .getAnnotation(CacheEvict.class);

        assertNotNull(evict);
        assertTrue(evict.allEntries());
        assertEquals(Set.of(CacheConfig.FEATURED_POSTS, CacheConfig.POST_SEARCH, CacheConfig.POST_SLUG),
            Set.of(evict.cacheNames()));
    }

    @Test
    void update_evictsPostCachesAllEntries() throws NoSuchMethodException {
        var evict = PostOutput.class
            .getDeclaredMethod("update", PostModel.class, List.class, List.class, AuthorModel.class)
            .getAnnotation(CacheEvict.class);

        assertNotNull(evict);
        assertTrue(evict.allEntries());
        assertEquals(Set.of(CacheConfig.FEATURED_POSTS, CacheConfig.POST_SEARCH, CacheConfig.POST_SLUG),
            Set.of(evict.cacheNames()));
    }

    @Test
    void deleteByIds_evictsPostCachesAllEntries() throws NoSuchMethodException {
        var evict = PostOutput.class
            .getDeclaredMethod("deleteByIds", List.class)
            .getAnnotation(CacheEvict.class);

        assertNotNull(evict);
        assertTrue(evict.allEntries());
        assertEquals(Set.of(CacheConfig.FEATURED_POSTS, CacheConfig.POST_SEARCH, CacheConfig.POST_SLUG),
            Set.of(evict.cacheNames()));
    }

    @Test
    void setFeaturedWeights_evictsPostCachesAllEntries() throws NoSuchMethodException {
        var evict = PostOutput.class
            .getDeclaredMethod("setFeaturedWeights", List.class, AuthorModel.class)
            .getAnnotation(CacheEvict.class);

        assertNotNull(evict);
        assertTrue(evict.allEntries());
        assertEquals(Set.of(CacheConfig.FEATURED_POSTS, CacheConfig.POST_SEARCH, CacheConfig.POST_SLUG),
            Set.of(evict.cacheNames()));
    }

    @Test
    void react_evictsPostSlugAllEntries() throws NoSuchMethodException {
        var evict = PostOutput.class
            .getDeclaredMethod("react", String.class, ReactionType.class, String.class)
            .getAnnotation(CacheEvict.class);

        assertNotNull(evict);
        assertTrue(evict.allEntries());
        assertEquals(Set.of(CacheConfig.POST_SLUG), Set.of(evict.cacheNames()));
    }

    private PaginatedInput<PostQueryModel> input(PostQueryModel query, String sort, Sort.Direction direction) {
        return new PaginatedInput<>(query, 0, 10, sort, direction);
    }
}
