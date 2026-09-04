package dev.vitorpaulo.blog.input.response;

import dev.vitorpaulo.blog.model.Language;

import java.util.Map;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String slug,
        String logoUrl,
        String programmingLanguage,
        Map<Language, ProjectContentResponse> translations
) {}
