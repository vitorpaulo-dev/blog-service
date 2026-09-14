package dev.vitorpaulo.blog.model;

import java.util.UUID;

public record FeaturedPostModel(
    UUID postId,
    Integer weight
) {}
