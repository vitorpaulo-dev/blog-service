package dev.vitorpaulo.blog.input.request;

import java.util.UUID;

public record PostQueryRequest(
	String query,
	UUID authorId
) {
}
