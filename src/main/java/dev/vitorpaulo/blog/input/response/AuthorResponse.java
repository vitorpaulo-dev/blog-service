package dev.vitorpaulo.blog.input.response;

import java.util.UUID;

public record AuthorResponse(
        UUID id,
        String slug,
        String name,
        String avatarUrl,
        String jobTitle
) {}
