package dev.vitorpaulo.blog.model.post;

import java.util.UUID;

public record Author(
        UUID id,
        String clerkUserId,
        String name,
        String avatarUrl,
        String jobTitle
) {}
