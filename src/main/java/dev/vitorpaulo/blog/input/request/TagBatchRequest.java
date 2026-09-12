package dev.vitorpaulo.blog.input.request;

import dev.vitorpaulo.blog.model.Language;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record TagBatchRequest(
    @NotEmpty List<UUID> ids,
    @NotNull Language language
) {}
