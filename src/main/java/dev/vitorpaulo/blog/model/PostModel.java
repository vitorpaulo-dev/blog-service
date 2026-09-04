package dev.vitorpaulo.blog.model;

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
        List<TagModel> tags,
        List<ProjectModel> projects,
        Long viewCount,
        Long loveCount,
        Long celebrateCount,
        Long geniusCount,
        Long helpCount,
        Long reactionCount,
        Map<Language, PostContentModel> translations
) {}
