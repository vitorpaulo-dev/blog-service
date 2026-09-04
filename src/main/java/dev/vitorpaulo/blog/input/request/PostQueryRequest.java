package dev.vitorpaulo.blog.input.request;

import dev.vitorpaulo.blog.model.Language;

import java.util.UUID;

public record PostQueryRequest(
    String query,
    UUID authorId,
    Language language
) {}
