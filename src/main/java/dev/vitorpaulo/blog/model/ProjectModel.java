package dev.vitorpaulo.blog.model;

import java.util.UUID;

public record ProjectModel(UUID id, String slug, String title, String logoUrl, String description, String programmingLanguage) {}
