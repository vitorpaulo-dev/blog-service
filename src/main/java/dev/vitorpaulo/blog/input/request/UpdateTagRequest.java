package dev.vitorpaulo.blog.input.request;

import dev.vitorpaulo.blog.model.Language;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record UpdateTagRequest(
    @NotNull @NotEmpty Map<Language, TagContentRequest> translations
) {}
