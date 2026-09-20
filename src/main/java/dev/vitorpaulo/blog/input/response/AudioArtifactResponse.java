package dev.vitorpaulo.blog.input.response;

import dev.vitorpaulo.blog.model.AudioStatus;

public record AudioArtifactResponse(
    AudioStatus status,
    String key,
    Integer progress,
    String error
) {}
