package dev.vitorpaulo.blog.input.request;

import dev.vitorpaulo.blog.model.Language;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProjectQueryRequest(
    String query,
    UUID authorId,
	@NotNull Language language,
    UUID tagId
) {}
