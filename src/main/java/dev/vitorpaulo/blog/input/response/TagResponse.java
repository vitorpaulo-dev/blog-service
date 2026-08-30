package dev.vitorpaulo.blog.input.response;

import java.util.UUID;

public record TagResponse(
        UUID id,
        String name,
        String slug,
        String description
) {}
