package dev.vitorpaulo.blog.input.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectContentRequest(
    @NotBlank @Size(max = 255) String title,
    String description
) {}
