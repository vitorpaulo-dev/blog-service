package dev.vitorpaulo.blog.model;

import java.util.Map;
import java.util.UUID;

public record TagModel(
    UUID id,
    String slug,
    Map<Language, TagContentModel> translations
) {}
