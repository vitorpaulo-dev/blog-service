package dev.vitorpaulo.blog.client.audio;

import dev.vitorpaulo.blog.model.AudioType;
import dev.vitorpaulo.blog.model.Language;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AudioJobRequest(
    UUID postId,
    String postSlug,
    List<AudioJobContent> contents,
    Map<AudioType, Map<Language, String>> uploads
) {

    public record AudioJobContent(
        Language language,
        String title,
        String content
    ) {}
}
