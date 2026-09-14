package dev.vitorpaulo.blog.model.upload;

public record PresignUploadRequestModel(
	String folder,
	String subfolder,
	String fileName
) {}
