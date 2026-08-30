package dev.vitorpaulo.blog.model.post;

import java.util.UUID;

public record Project(UUID id, String slug, String title, String logoUrl, String description, String programmingLanguage) {}
