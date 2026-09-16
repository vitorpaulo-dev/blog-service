package dev.vitorpaulo.blog.input.request;

import dev.vitorpaulo.blog.model.Frequency;
import dev.vitorpaulo.blog.model.Language;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SubscribeRequest(
    @NotBlank @Email String email,
    @NotNull Language language,
    @NotNull Frequency frequency
) {}
