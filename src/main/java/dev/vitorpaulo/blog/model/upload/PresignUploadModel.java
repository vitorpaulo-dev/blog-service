package dev.vitorpaulo.blog.model.upload;

public record PresignUploadModel(
	String key,
	String uploadUrl
) {}
