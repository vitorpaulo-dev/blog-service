package dev.vitorpaulo.blog.input.request;

import dev.vitorpaulo.blog.model.Language;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record CreateProjectRequest(
    @Size(max = 1024) String logoUrl,
    @Size(max = 1024) String bannerUrl,
    @Size(max = 1024) String githubUrl,
    @Size(max = 1024) String websiteUrl,
    String programmingLanguage,
    @NotEmpty Map<Language, ProjectContentRequest> translations,
    String status
) {}
