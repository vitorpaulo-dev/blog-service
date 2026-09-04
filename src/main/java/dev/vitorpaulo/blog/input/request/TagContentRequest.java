package dev.vitorpaulo.blog.input.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagContentRequest(
    @NotBlank @Size(max = 255) String name,
    @Size(max = 1024) String description
) {}
