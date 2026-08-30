package dev.vitorpaulo.blog.model.post;

import java.util.UUID;

public record Tag(UUID id, String name, String slug, String description) {}
