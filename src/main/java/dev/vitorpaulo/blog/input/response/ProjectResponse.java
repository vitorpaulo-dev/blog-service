package dev.vitorpaulo.blog.input.response;

import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String slug,
        String title,
        String logoUrl,
        String description,
        String programmingLanguage
) {}
