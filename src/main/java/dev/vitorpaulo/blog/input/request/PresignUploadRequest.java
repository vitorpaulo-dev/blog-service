package dev.vitorpaulo.blog.input.request;

import jakarta.validation.constraints.NotBlank;

public record PresignUploadRequest(
	@NotBlank String folder,
	@NotBlank String subfolder,
	@NotBlank String fileName
) {}
