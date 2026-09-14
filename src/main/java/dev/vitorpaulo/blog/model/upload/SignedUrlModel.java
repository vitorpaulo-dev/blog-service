package dev.vitorpaulo.blog.model.upload;

public record SignedUrlModel(
	String key,
	String url
) {}
