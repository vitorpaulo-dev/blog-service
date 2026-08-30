package dev.vitorpaulo.blog.input.response;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record PostResponse(
        UUID id,
        String slug,
        String title,
        String bannerUrl,
        String content,
        String language,
        String status,
        Long viewCount,
        Integer averageReadingTimeSeconds,
        Integer estimatedReadingTimeMinutes,
        OffsetDateTime lastViewedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Set<AuthorResponse> authors,
        Set<TagResponse> tags,
        Set<ProjectResponse> projects,
        Long reactionCount,
        Long commentCount
) {}
