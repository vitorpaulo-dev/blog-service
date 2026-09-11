package dev.vitorpaulo.blog.input.request;

import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.ProjectStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record UpdateProjectRequest(
    @Size(max = 1024) String logoUrl,
    @Size(max = 1024) String bannerUrl,
    @Size(max = 1024) String githubUrl,
    @Size(max = 1024) String websiteUrl,
    String programmingLanguage,
    @NotEmpty Map<Language, ProjectContentRequest> translations,
    @NotNull ProjectStatus status
) {}
