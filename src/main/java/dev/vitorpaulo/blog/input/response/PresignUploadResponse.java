package dev.vitorpaulo.blog.input.response;

public record PresignUploadResponse(
	String uploadUrl,
	String key
) {}
