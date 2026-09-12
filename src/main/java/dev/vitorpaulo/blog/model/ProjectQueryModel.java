package dev.vitorpaulo.blog.model;

import java.util.UUID;

public record ProjectQueryModel(
    String query,
    UUID authorId,
    Language language,
    UUID tagId
) {}
