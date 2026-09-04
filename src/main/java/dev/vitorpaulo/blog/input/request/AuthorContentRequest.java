package dev.vitorpaulo.blog.input.request;

import jakarta.validation.constraints.Size;

public record AuthorContentRequest(
    String bio,
    @Size(max = 255) String jobTitle
) {}
