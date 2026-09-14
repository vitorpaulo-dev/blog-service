package dev.vitorpaulo.blog.input.request;

import java.util.UUID;

public record FeaturedPostRequest(
    UUID postId,
    Integer weight
) {}
