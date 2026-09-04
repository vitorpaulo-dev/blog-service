package dev.vitorpaulo.blog.model;

import java.util.Map;
import java.util.UUID;

public record ProjectModel(
    UUID id,
    String slug,
    String logoUrl,
    String programmingLanguage,
    Map<Language, ProjectContentModel> translations
) {}
