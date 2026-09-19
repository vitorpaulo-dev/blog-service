package dev.vitorpaulo.blog.output.audio;

import dev.vitorpaulo.blog.client.audio.AudioJobRequest;
import dev.vitorpaulo.blog.client.audio.AudioWorkerFeignClient;
import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.domain.AudioEntity;
import dev.vitorpaulo.blog.domain.PostContentEntity;
import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.model.AudioStatus;
import dev.vitorpaulo.blog.model.AudioType;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostStatus;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.model.upload.SignedUrlModel;
import dev.vitorpaulo.blog.output.mapper.AudioOutputMapper;
import dev.vitorpaulo.blog.output.mapper.AudioOutputMapperImpl;
import dev.vitorpaulo.blog.output.upload.StorageOutput;
import dev.vitorpaulo.blog.repository.AudioRepository;
import dev.vitorpaulo.blog.repository.PostRepository;
import dev.vitorpaulo.blog.repository.RedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AudioOutputTest {

    @Mock private AudioRepository audioRepository;
    @Mock private PostRepository postRepository;
    @Mock private StorageOutput storageOutput;
    @Mock private AudioWorkerFeignClient audioWorkerFeignClient;
    @Mock private RedisRepository redisRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private AudioOutput audioOutput;

    private UUID postId;

    private PostModel postModel;

    @BeforeEach
    void setUp() {
        audioOutput = new AudioOutput(audioRepository, postRepository, storageOutput,
            audioWorkerFeignClient, redisRepository, objectMapper, new AudioOutputMapperImpl());
        postId = UUID.randomUUID();
        postModel = mockPostModel(postId);
        when(redisRepository.getValue(anyString())).thenReturn(Optional.empty());
    }

    private PostModel mockPostModel(UUID id) {
        var model = org.mockito.Mockito.mock(PostModel.class);
        org.mockito.Mockito.when(model.id()).thenReturn(id);
        return model;
    }

    @Test
    void dispatch_publishedPost_savesFourQueuedArtifactsAndSendsJob() {
        when(audioRepository.findByPostIdAndTypeAndLanguage(eq(postId), any(), any())).thenReturn(Optional.empty());
        stubArtifactSave();
        stubPresign();

        audioOutput.dispatch(publishedPost());

        verify(audioRepository, never()).delete(any(AudioEntity.class));
        verify(storageOutput, never()).delete(anyString());

        var saved = ArgumentCaptor.forClass(AudioEntity.class);
        verify(audioRepository, times(8)).save(saved.capture());
        assertEquals(8, saved.getAllValues().size());
        saved.getAllValues().forEach(artifact -> {
            assertEquals(postId, artifact.getPostId());
            assertEquals(AudioStatus.QUEUED, artifact.getStatus());
            assertNull(artifact.getErrorMessage());
            assertEquals(expectedHash(artifact.getLanguage()), artifact.getContentHash());
        });

        var job = ArgumentCaptor.forClass(AudioJobRequest.class);
        verify(audioWorkerFeignClient).generate(job.capture());
        assertEquals(postId, job.getValue().postId());
        assertEquals("my-post", job.getValue().postSlug());
        assertEquals(2, job.getValue().contents().size());
        assertEquals(
            List.of(
                new AudioJobRequest.AudioJobContent(Language.ENGLISH, "Hello", "# Hello world"),
                new AudioJobRequest.AudioJobContent(Language.PORTUGUESE, "Ola", "# Ola mundo")
            ),
            job.getValue().contents()
        );
        assertEquals(4, job.getValue().uploads().entrySet().stream()
            .flatMap(type -> type.getValue().values().stream())
            .count());
    }

    @Test
    void dispatch_existingArtifacts_deletesOldR2ObjectsAndRedispatches() {
        var oldKey = "post/audio/" + postId + "/old-narration.wav";
        when(audioRepository.findByPostIdAndTypeAndLanguage(eq(postId), any(), any()))
            .thenAnswer(invocation -> Optional.of(artifact(
                invocation.getArgument(1), invocation.getArgument(2), oldKey, AudioStatus.READY)));
        stubArtifactSave();
        stubPresign();

        audioOutput.dispatch(publishedPost());
        verifyNoInteractions(postRepository);

        verify(storageOutput, times(4)).delete(oldKey);
        var saved = ArgumentCaptor.forClass(AudioEntity.class);
        verify(audioRepository, times(4)).save(saved.capture());
        saved.getAllValues().forEach(artifact -> assertEquals(AudioStatus.QUEUED, artifact.getStatus()));
        verify(audioWorkerFeignClient).generate(any(AudioJobRequest.class));
    }

    @Test
    void dispatch_unchangedContentHash_skipsArtifactsEntirely() {
        when(audioRepository.findByPostIdAndTypeAndLanguage(eq(postId), any(), any()))
            .thenAnswer(invocation -> {
                var type = (AudioType) invocation.getArgument(1);
                var language = (Language) invocation.getArgument(2);
                var stored = artifact(type, language, keyFor(type, language), AudioStatus.READY);
                stored.setContentHash(expectedHash(language));
                return Optional.of(stored);
            });

        audioOutput.dispatch(publishedPost());

        verifyNoInteractions(audioWorkerFeignClient, storageOutput);
        verify(audioRepository, never()).save(any(AudioEntity.class));
    }

    @Test
    void dispatch_singleArtifactContentChanged_redispatchesOnlyThatArtifact() {
        when(audioRepository.findByPostIdAndTypeAndLanguage(eq(postId), any(), any()))
            .thenAnswer(invocation -> {
                var type = (AudioType) invocation.getArgument(1);
                var language = (Language) invocation.getArgument(2);
                var stored = artifact(type, language, keyFor(type, language), AudioStatus.READY);
                var contentHash = type == AudioType.NARRATION && language == Language.PORTUGUESE
                    ? "stale-hash"
                    : expectedHash(language);
                stored.setContentHash(contentHash);
                return Optional.of(stored);
            });
        stubArtifactSave();
        stubPresign();

        audioOutput.dispatch(publishedPost());

        var job = ArgumentCaptor.forClass(AudioJobRequest.class);
        verify(audioWorkerFeignClient).generate(job.capture());
        assertEquals(Map.of(AudioType.NARRATION, Map.of(Language.PORTUGUESE,
            "https://put/NARRATION-PORTUGUESE.wav")), job.getValue().uploads());

        verify(storageOutput).delete(keyFor(AudioType.NARRATION, Language.PORTUGUESE));
        verify(storageOutput, times(1)).presignUpload(anyString(), anyString(), anyString());
        var saved = ArgumentCaptor.forClass(AudioEntity.class);
        verify(audioRepository).save(saved.capture());
        assertEquals(AudioType.NARRATION, saved.getValue().getType());
        assertEquals(Language.PORTUGUESE, saved.getValue().getLanguage());
        assertEquals(AudioStatus.QUEUED, saved.getValue().getStatus());
        assertEquals(expectedHash(Language.PORTUGUESE), saved.getValue().getContentHash());
    }

    @Test
    void dispatch_allUnchanged_sendsNoWorkerJob() {
        when(audioRepository.findByPostIdAndTypeAndLanguage(eq(postId), any(), any()))
            .thenAnswer(invocation -> {
                var type = (AudioType) invocation.getArgument(1);
                var language = (Language) invocation.getArgument(2);
                var stored = artifact(type, language, keyFor(type, language), AudioStatus.READY);
                stored.setContentHash(expectedHash(language));
                return Optional.of(stored);
            });

        audioOutput.dispatch(publishedPost());

        verify(audioWorkerFeignClient, never()).generate(any(AudioJobRequest.class));
    }

    @Test
    void dispatch_draftPost_doesNothing() {

        audioOutput.dispatch(post(PostStatus.DRAFT));

        verifyNoInteractions(audioWorkerFeignClient, storageOutput, audioRepository, postRepository);
    }

    @Test
    void dispatch_workerFails_doesNotPropagate() {
        stubArtifactSave();
        stubPresign();
        org.mockito.Mockito.doThrow(new RuntimeException("worker down"))
            .when(audioWorkerFeignClient).generate(any());

        audioOutput.dispatch(publishedPost());

        verify(audioWorkerFeignClient).generate(any());
    }

    @Test
    void artifacts_mergesRedisProgressIntoDbRowsAndMapsModels() {
        var narration = artifact(AudioType.NARRATION, Language.ENGLISH, "post/audio/1/narration.wav",
            AudioStatus.GENERATING);
        var podcast = artifact(AudioType.PODCAST, Language.PORTUGUESE, "post/audio/1/podcast.wav", AudioStatus.QUEUED);
        when(audioRepository.findByPostId(postId)).thenReturn(List.of(narration, podcast));
        when(redisRepository.getValue(postId + ":NARRATION:ENGLISH"))
            .thenReturn(Optional.of("{\"status\":\"GENERATING\",\"progress\":42,\"error\":null}"));
        when(redisRepository.getValue(postId + ":PODCAST:PORTUGUESE"))
            .thenReturn(Optional.of("{\"status\":\"FAILED\",\"progress\":10,\"error\":\"render failed\"}"));

        var models = audioOutput.artifacts(postId);

        assertEquals(2, models.size());
        var first = models.get(0);
        assertEquals(AudioType.NARRATION, first.type());
        assertEquals(Language.ENGLISH, first.language());
        assertEquals(AudioStatus.GENERATING, first.status());
        assertEquals(42, first.progress());
        assertNull(first.error());

        var second = models.get(1);
        assertEquals(AudioStatus.FAILED, second.status());
        assertEquals("render failed", second.error());
    }

    @Test
    void artifacts_withoutRedisValue_fallsBackToDbState() {
        var narration = artifact(AudioType.NARRATION, Language.ENGLISH, "post/audio/1/narration.wav", AudioStatus.QUEUED);
        when(audioRepository.findByPostId(postId)).thenReturn(List.of(narration));
        when(redisRepository.getValue(anyString())).thenReturn(Optional.empty());

        var models = audioOutput.artifacts(postId);

        assertEquals(AudioStatus.QUEUED, models.getFirst().status());
        assertNull(models.getFirst().progress());
        assertNull(models.getFirst().error());
    }

    @Test
    void artifactMap_groupsAllArtifactsByTypeAndLanguage() {
        var narrationEn = artifact(AudioType.NARRATION, Language.ENGLISH, "post/audio/1/narration.wav", AudioStatus.READY);
        var narrationPt = artifact(AudioType.NARRATION, Language.PORTUGUESE, "post/audio/2/narration.wav", AudioStatus.GENERATING);
        var podcastEn = artifact(AudioType.PODCAST, Language.ENGLISH, "post/audio/3/podcast.wav", AudioStatus.FAILED);
        when(audioRepository.findByPostId(postId)).thenReturn(List.of(narrationEn, narrationPt, podcastEn));
        when(redisRepository.getValue(anyString())).thenReturn(Optional.empty());

        var full = audioOutput.artifactMap(postModel);
        verifyNoInteractions(postRepository);

        assertEquals(2, full.get(AudioType.NARRATION).size());
        assertEquals(AudioStatus.READY, full.get(AudioType.NARRATION).get(Language.ENGLISH).status());
        assertEquals(AudioStatus.GENERATING, full.get(AudioType.NARRATION).get(Language.PORTUGUESE).status());
        assertEquals(AudioStatus.FAILED, full.get(AudioType.PODCAST).get(Language.ENGLISH).status());
        assertNull(full.get(AudioType.PODCAST).get(Language.PORTUGUESE));
    }

    @Test
    void artifactMap_userLanguage_returnsOnlyRequestedLanguage() {
        var narrationEn = artifact(AudioType.NARRATION, Language.ENGLISH, "post/audio/1/narration.wav", AudioStatus.READY);
        var narrationPt = artifact(AudioType.NARRATION, Language.PORTUGUESE, "post/audio/2/narration.wav", AudioStatus.GENERATING);
        when(audioRepository.findByPostId(postId)).thenReturn(List.of(narrationEn, narrationPt));
        when(redisRepository.getValue(anyString())).thenReturn(Optional.empty());

        var filtered = audioOutput.artifactMap(postModel, Language.PORTUGUESE);

        assertNull(filtered.get(AudioType.NARRATION).get(Language.ENGLISH));
        assertEquals(AudioStatus.GENERATING, filtered.get(AudioType.NARRATION).get(Language.PORTUGUESE).status());
    }

    @Test
    void artifactMap_noArtifacts_returnsEmptyMap() {
        when(audioRepository.findByPostId(postId)).thenReturn(List.of());
        when(redisRepository.getValue(anyString())).thenReturn(Optional.empty());

        assertTrue(audioOutput.artifactMap(postModel).isEmpty());
    }

    @Test
    void retry_generatingArtifact_conflicts() {
        when(postRepository.findByIdWithContents(postId)).thenReturn(Optional.of(publishedPost()));
        var artifact = artifact(AudioType.NARRATION, Language.ENGLISH, "post/audio/1/narration.wav", AudioStatus.GENERATING);
        when(audioRepository.findByPostIdAndTypeAndLanguage(postId, AudioType.NARRATION, Language.ENGLISH))
            .thenReturn(Optional.of(artifact));

        var exception = assertThrows(BusinessException.class,
            () -> audioOutput.retry(postId, AudioType.NARRATION, Language.ENGLISH));

        assertEquals(409, exception.getStatus().value());
        verifyNoInteractions(audioWorkerFeignClient, storageOutput);
    }

    @Test
    void retry_failedArtifact_deletesOldObjectAndDispatchesSingleArtifactQueued() {
        var oldKey = "post/audio/1/narration.wav";
        var artifact = artifact(AudioType.NARRATION, Language.ENGLISH, oldKey, AudioStatus.FAILED, "boom");
        when(postRepository.findByIdWithContents(postId)).thenReturn(Optional.of(publishedPost()));
        when(audioRepository.findByPostIdAndTypeAndLanguage(postId, AudioType.NARRATION, Language.ENGLISH))
            .thenReturn(Optional.of(artifact));
        stubArtifactSave();
        stubPresign();

        var model = audioOutput.retry(postId, AudioType.NARRATION, Language.ENGLISH);

        assertEquals(AudioStatus.QUEUED, model.status());
        var saved = ArgumentCaptor.forClass(AudioEntity.class);
        verify(audioRepository).save(saved.capture());
        assertEquals(AudioStatus.QUEUED, saved.getValue().getStatus());
        assertTrue(saved.getValue().getR2Key().startsWith("post/audio/" + postId + "/"));
        assertEquals(expectedHash(Language.ENGLISH), saved.getValue().getContentHash());

        var job = ArgumentCaptor.forClass(AudioJobRequest.class);
        verify(audioWorkerFeignClient).generate(job.capture());
        assertEquals(Map.of(AudioType.NARRATION, Map.of(Language.ENGLISH, "https://put/NARRATION-ENGLISH.wav")), job.getValue().uploads());
        assertEquals(2, job.getValue().contents().size());
    }

    @Test
    void retry_missingArtifact_notFound() {
        when(postRepository.findByIdWithContents(postId)).thenReturn(Optional.of(publishedPost()));
        when(audioRepository.findByPostIdAndTypeAndLanguage(postId, AudioType.NARRATION, Language.ENGLISH))
            .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
            () -> audioOutput.retry(postId, AudioType.NARRATION, Language.ENGLISH));

        verifyNoInteractions(audioWorkerFeignClient, storageOutput);
    }

    private void stubArtifactSave() {
        when(audioRepository.save(any(AudioEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private String expectedHash(Language language) {
        var content = language == Language.ENGLISH ? englishContent() : portugueseContent();
        return AudioOutput.contentHash(content.getTitle(), content.getContent());
    }

    private String keyFor(AudioType type, Language language) {
        return "post/audio/" + postId + "/" + type + "-" + language + ".wav";
    }

    private void stubPresign() {
        when(storageOutput.presignUpload(anyString(), anyString(), anyString()))
            .thenAnswer(invocation -> new SignedUrlModel(
                "post/audio/" + invocation.getArgument(1) + "/" + invocation.<String>getArgument(2),
                "https://put/" + invocation.<String>getArgument(2)
            ));
    }

    private PostEntity publishedPost() {
        return post(PostStatus.PUBLISHED);
    }

    private PostEntity post(PostStatus status) {
        var post = new PostEntity();
        post.setId(postId);
        post.setSlug("my-post");
        post.setStatus(status);
        post.setContents(List.of(englishContent(), portugueseContent()));
        return post;
    }

    private PostContentEntity englishContent() {
        return content(Language.ENGLISH, "Hello", "# Hello world");
    }

    private PostContentEntity portugueseContent() {
        return content(Language.PORTUGUESE, "Ola", "# Ola mundo");
    }

    private PostContentEntity content(Language language, String title, String content) {
        var entity = new PostContentEntity();
        entity.setLanguage(language);
        entity.setTitle(title);
        entity.setContent(content);
        return entity;
    }

    private AudioEntity artifact(AudioType type, Language language, String r2Key, AudioStatus status) {
        return artifact(type, language, r2Key, status, null);
    }

    private AudioEntity artifact(AudioType type, Language language, String r2Key, AudioStatus status, String errorMessage) {
        return AudioEntity.builder()
            .postId(postId)
            .type(type)
            .language(language)
            .status(status)
            .r2Key(r2Key)
            .errorMessage(errorMessage)
            .build();
    }
}
