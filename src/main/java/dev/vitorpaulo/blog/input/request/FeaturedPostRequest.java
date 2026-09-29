package dev.vitorpaulo.blog.input.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FeaturedPostRequest(
    @NotNull UUID postId,
    Integer weight
) {}
