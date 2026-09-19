package dev.vitorpaulo.blog.output.audio;

import dev.vitorpaulo.blog.client.audio.AudioJobRequest;
import dev.vitorpaulo.blog.client.audio.AudioWorkerFeignClient;
import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.domain.AudioEntity;
import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.model.AudioStatus;
import dev.vitorpaulo.blog.model.AudioType;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostStatus;
import dev.vitorpaulo.blog.model.PostModel;
import dev.vitorpaulo.blog.model.audio.AudioModel;
import dev.vitorpaulo.blog.model.audio.AudioProgressModel;
import dev.vitorpaulo.blog.output.mapper.AudioOutputMapper;
import dev.vitorpaulo.blog.output.upload.StorageOutput;
import dev.vitorpaulo.blog.repository.AudioRepository;
import dev.vitorpaulo.blog.repository.PostRepository;
import dev.vitorpaulo.blog.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AudioOutput {

    private static final String AUDIO_FOLDER = "post/audio";
    private static final String AUDIO_EXTENSION = ".wav";

    private final AudioRepository audioRepository;
    private final PostRepository postRepository;
    private final StorageOutput storageOutput;
    private final AudioWorkerFeignClient audioWorkerFeignClient;
    private final RedisRepository redisRepository;
    private final ObjectMapper objectMapper;
    private final AudioOutputMapper audioOutputMapper;

    @Transactional(readOnly = true)
    public List<AudioModel> artifacts(UUID postId) {
        return audioRepository.findByPostId(postId).stream()
            .map(this::toModel)
            .toList();
    }

    @Transactional
    public AudioModel retry(UUID postId, AudioType type, Language language) {
        final var post = postEntity(postId);
        final var artifact = audioRepository.findByPostIdAndTypeAndLanguage(postId, type, language)
            .orElseThrow(() -> new NotFoundException(ExceptionCode.AUDIO_NOT_FOUND));

        final var model = toModel(artifact);
        if (model.status() == AudioStatus.GENERATING) {
            throw new BusinessException(HttpStatus.CONFLICT, ExceptionCode.AUDIO_GENERATING, null);
        }

        final var url = renewArtifact(artifact, contentHash(post, language));
        audioWorkerFeignClient.generate(new AudioJobRequest(
            postId,
            post.getSlug(),
            audioOutputMapper.toJobContents(post.getContents()),
            Map.of(type, Map.of(language, url))
        ));

        return toModel(artifact);
    }

    @Transactional(readOnly = true)
    public Map<AudioType, Map<Language, AudioModel>> artifactMap(PostModel post) {
        final var full = new EnumMap<AudioType, Map<Language, AudioModel>>(AudioType.class);

        for (var model : artifacts(post.id())) {
            final var byLanguage = full.computeIfAbsent(model.type(), type -> new EnumMap<>(Language.class));
            byLanguage.put(model.language(), model);
        }

        return full;
    }

    @Transactional(readOnly = true)
    public Map<AudioType, Map<Language, AudioModel>> artifactMap(PostModel post, Language language) {
        final var filtered = new EnumMap<AudioType, Map<Language, AudioModel>>(AudioType.class);

        for (var model : artifacts(post.id())) {
            if (model.language() == language) {
                filtered.put(model.type(), new EnumMap<>(Map.of(language, model)));
            }
        }

        return filtered;
    }

    @Transactional
    public void dispatch(PostEntity post) {
        try {
            if (post.getStatus() != PostStatus.PUBLISHED) {
                return;
            }

            final var postId = post.getId();
            final var uploads = new EnumMap<AudioType, Map<Language, String>>(AudioType.class);
            for (var type : AudioType.values()) {
                for (var language : Language.values()) {
                    final var hash = contentHash(post, language);
                    final var existing = audioRepository.findByPostIdAndTypeAndLanguage(postId, type, language)
                        .orElse(null);

                    if (existing != null && hash.equals(existing.getContentHash())) {
                        continue;
                    }

                    final var artifact = existing != null
                        ? existing
                        : audioRepository.save(audioOutputMapper.toEntity(postId, type, language));

                    uploads.computeIfAbsent(type, ignored -> new EnumMap<>(Language.class))
                        .put(language, renewArtifact(artifact, hash));
                }
            }

            if (!uploads.isEmpty()) {
                audioWorkerFeignClient.generate(new AudioJobRequest(
                    postId,
                    post.getSlug(),
                    audioOutputMapper.toJobContents(post.getContents()),
                    uploads
                ));
            }
        } catch (Exception e) {
            log.error("Failed to dispatch audio job for post {}", post.getId(), e);
        }
    }

    private String renewArtifact(AudioEntity artifact, String contentHash) {
        final var uploaded = storageOutput.presignUpload(
            AUDIO_FOLDER,
            artifact.getPostId().toString(),
            artifact.getType().name() + "-" + artifact.getLanguage().name() + AUDIO_EXTENSION
        );

        if (artifact.getR2Key() != null) {
            try {
                storageOutput.delete(artifact.getR2Key());
            } catch (BusinessException e) {
                log.warn("Failed to delete stale audio object {} for post {}", artifact.getR2Key(), artifact.getPostId(), e);
            }
        }

        artifact.setR2Key(uploaded.key());
        artifact.setStatus(AudioStatus.QUEUED);
        artifact.setContentHash(contentHash);
        artifact.setErrorMessage(null);
        audioRepository.save(artifact);

        return uploaded.url();
    }

    static String contentHash(String title, String content) {
        try {
            final var digest = MessageDigest.getInstance("SHA-256");
            final var bytes = digest.digest((title + "\n" + content).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }

    private String contentHash(PostEntity post, Language language) {
        return post.getContents().stream()
            .filter(content -> content.getLanguage() == language)
            .findFirst()
            .map(content -> contentHash(content.getTitle(), content.getContent()))
            .orElseThrow(() -> new IllegalStateException("Missing audio content for language " + language));
    }

    private PostEntity postEntity(UUID postId) {
        return postRepository.findByIdWithContents(postId)
            .orElseThrow(() -> new NotFoundException(ExceptionCode.POST_NOT_FOUND));
    }

    private AudioModel toModel(AudioEntity artifact) {
        return progress(artifact)
            .map(progress -> audioOutputMapper.toModel(artifact, progress))
            .orElseGet(() -> audioOutputMapper.toModel(artifact));
    }

    private Optional<AudioProgressModel> progress(AudioEntity artifact) {
        final var key = artifact.getPostId() + ":" + artifact.getType() + ":" + artifact.getLanguage();
        return redisRepository.getValue(key)
            .flatMap(this::readProgress);
    }

    private Optional<AudioProgressModel> readProgress(String value) {
        try {
            return Optional.of(objectMapper.readValue(value, AudioProgressModel.class));
        } catch (Exception e) {
            log.warn("Ignoring unreadable audio progress payload: {}", value);
            return Optional.empty();
        }
    }
}
