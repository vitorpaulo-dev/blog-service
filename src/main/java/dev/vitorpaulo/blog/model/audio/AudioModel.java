package dev.vitorpaulo.blog.model.audio;

import dev.vitorpaulo.blog.model.AudioStatus;
import dev.vitorpaulo.blog.model.AudioType;
import dev.vitorpaulo.blog.model.Language;

public record AudioModel(
    AudioType type,
    Language language,
    AudioStatus status,
    String key,
    String error,
    Integer progress
) {}
