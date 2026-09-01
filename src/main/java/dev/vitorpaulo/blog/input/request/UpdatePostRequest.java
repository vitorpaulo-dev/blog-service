package dev.vitorpaulo.blog.input.request;

import dev.vitorpaulo.blog.model.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record UpdatePostRequest(
        @NotBlank @Size(max = 500) String title,
        @Size(max = 1024) String bannerUrl,
        @NotBlank String content,
        @Size(max = 10) String language,
        List<UUID> tagIds,
        List<UUID> projectIds,
        @NotNull PostStatus status
) {}
