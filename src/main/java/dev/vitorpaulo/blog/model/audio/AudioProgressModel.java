package dev.vitorpaulo.blog.model.audio;

import dev.vitorpaulo.blog.model.AudioType;
import dev.vitorpaulo.blog.model.Language;

public record AudioProgressModel(
    String status,
    Integer progress,
    String error
) {}
