package dev.vitorpaulo.blog.input.response;

import dev.vitorpaulo.blog.model.Language;

import java.util.Map;
import java.util.UUID;

public record TagResponse(
        UUID id,
        String slug,
        Map<Language, TagContentResponse> translations
) {}
