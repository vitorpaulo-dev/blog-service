package dev.vitorpaulo.blog.input.response;

import dev.vitorpaulo.blog.model.Language;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PostResponse(
        UUID id,
        String slug,
        String bannerUrl,
        String status,
        Integer estimatedReading,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<AuthorResponse> authors,
        List<UUID> tagIds,
        List<UUID> projectIds,
        Long viewCount,
        Long loveCount,
        Long celebrateCount,
        Long geniusCount,
        Long helpCount,
        Long reactionCount,
        Integer weight,
        Map<Language, PostContentResponse> translations
) {}
