package dev.vitorpaulo.blog.input.response;

import dev.vitorpaulo.blog.model.AudioStatus;
import dev.vitorpaulo.blog.model.AudioType;
import dev.vitorpaulo.blog.model.Language;

public record AudioResponse(
    AudioType type,
    Language language,
    AudioStatus status,
    String key,
    String error,
    Integer progress
) {}
