package dev.vitorpaulo.blog.input.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TopItemResponse(
    UUID id,
    String title,
    String slug,
    long viewCount,
    long reactionCount,
    OffsetDateTime createdAt
) {}
