package dev.vitorpaulo.blog.input.request;

import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record UpdatePostRequest(
        @Size(max = 1024) String bannerUrl,
        @NotNull @NotEmpty Map<Language, PostContentRequest> translations,
        List<UUID> tagIds,
        List<UUID> projectIds,
        @NotNull PostStatus status
) {}
