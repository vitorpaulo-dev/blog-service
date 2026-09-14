package dev.vitorpaulo.blog.input.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SignUrlsRequest(
	@NotEmpty List<@NotBlank String> keys
) {}
