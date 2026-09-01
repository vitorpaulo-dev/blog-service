package dev.vitorpaulo.blog.model;

import java.util.UUID;

public record TagModel(UUID id, String name, String slug, String description) {}
