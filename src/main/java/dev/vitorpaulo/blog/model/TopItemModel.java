package dev.vitorpaulo.blog.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TopItemModel(
    UUID id,
    String title,
    String slug,
    long viewCount,
    long reactionCount,
    OffsetDateTime createdAt
) {}
