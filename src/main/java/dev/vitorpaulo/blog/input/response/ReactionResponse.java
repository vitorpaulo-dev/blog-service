package dev.vitorpaulo.blog.input.response;

public record ReactionResponse(
	Long loveCount,
	Long celebrateCount,
	Long geniusCount,
	Long helpCount,
	Long reactionCount
) {}
