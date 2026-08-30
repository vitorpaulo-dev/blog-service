package dev.vitorpaulo.blog.input.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record MassDeleteRequest(
        @NotEmpty List<UUID> ids
) {}
