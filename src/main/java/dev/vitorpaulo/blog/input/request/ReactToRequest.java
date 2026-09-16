package dev.vitorpaulo.blog.input.request;

import dev.vitorpaulo.blog.model.ReactionType;
import jakarta.validation.constraints.NotNull;

public record ReactToRequest(
	@NotNull ReactionType reactionType
) {}
