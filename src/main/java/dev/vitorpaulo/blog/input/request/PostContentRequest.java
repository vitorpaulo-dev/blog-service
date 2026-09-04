package dev.vitorpaulo.blog.input.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostContentRequest(
    @NotBlank @Size(max = 500) String title,
    @NotBlank String content
) {}
