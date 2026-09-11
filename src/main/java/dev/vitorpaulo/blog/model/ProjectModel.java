package dev.vitorpaulo.blog.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProjectModel(
    UUID id,
    String slug,
    String logoUrl,
    String programmingLanguage,
    String bannerUrl,
    String githubUrl,
    String websiteUrl,
    ProjectStatus status,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<AuthorModel> authors,
    Long viewCount,
    Long loveCount,
    Long celebrateCount,
    Long geniusCount,
    Long helpCount,
    Long reactionCount,
    Map<Language, ProjectContentModel> translations
) {}
