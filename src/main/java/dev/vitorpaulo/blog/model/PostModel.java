package dev.vitorpaulo.blog.model;

import dev.vitorpaulo.blog.model.audio.AudioModel;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PostModel(
        UUID id,
        String slug,
        String bannerUrl,
        PostStatus status,
        Integer estimatedReading,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<AuthorModel> authors,
        List<UUID> tagIds,
        List<UUID> projectIds,
        Long viewCount,
        Long loveCount,
        Long celebrateCount,
        Long geniusCount,
        Long helpCount,
        Long reactionCount,
        Integer weight,
        Map<Language, PostContentModel> translations,
        Map<AudioType, Map<Language, AudioModel>> audio
) {
    public PostModel withAudio(Map<AudioType, Map<Language, AudioModel>> audio) {
        return new PostModel(
            id, slug, bannerUrl, status, estimatedReading, createdAt, updatedAt,
            authors, tagIds, projectIds, viewCount, loveCount, celebrateCount,
            geniusCount, helpCount, reactionCount, weight, translations, audio
        );
    }
}
