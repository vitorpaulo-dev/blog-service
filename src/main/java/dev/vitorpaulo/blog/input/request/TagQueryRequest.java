package dev.vitorpaulo.blog.input.request;

import dev.vitorpaulo.blog.model.Language;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TagQueryRequest(
    @Size(max = 255) String name,
    @NotNull Language language
) {}
