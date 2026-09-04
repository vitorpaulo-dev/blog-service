package dev.vitorpaulo.blog.model;

import java.util.Map;
import java.util.UUID;

public record AuthorModel(
        UUID id,
        String clerkUserId,
        String name,
        String slug,
        String avatarUrl,
        String role,
        Map<Language, AuthorContentModel> translations
) {}
