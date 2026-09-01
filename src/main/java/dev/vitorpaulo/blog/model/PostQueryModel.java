package dev.vitorpaulo.blog.model;

import java.util.UUID;

public record PostQueryModel(
	String query,
	UUID authorId
) {
}
