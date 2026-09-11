package dev.vitorpaulo.blog.input.response;

import dev.vitorpaulo.blog.model.Language;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String slug,
        String logoUrl,
        String bannerUrl,
        String githubUrl,
        String websiteUrl,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<AuthorResponse> authors,
        List<TagResponse> tags,
        Long viewCount,
        Long loveCount,
        Long celebrateCount,
        Long geniusCount,
        Long helpCount,
        Long reactionCount,
        Map<Language, ProjectContentResponse> translations
) {}
