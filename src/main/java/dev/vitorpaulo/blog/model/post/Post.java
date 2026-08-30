package dev.vitorpaulo.blog.model.post;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record Post(
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
        Set<Author> authors,
        Set<Tag> tags,
        Set<Project> projects,
        Long reactionCount,
        Long commentCount
) {}
