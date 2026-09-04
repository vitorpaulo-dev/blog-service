package dev.vitorpaulo.blog.input.response;

import dev.vitorpaulo.blog.model.Language;

import java.util.Map;
import java.util.UUID;

public record AuthorResponse(
        UUID id,
        String slug,
        String name,
        String avatarUrl,
        Map<Language, AuthorContentResponse> translations
) {}
