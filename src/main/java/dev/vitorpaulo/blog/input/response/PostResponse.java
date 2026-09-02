package dev.vitorpaulo.blog.input.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record PostResponse(
        UUID id,
        String slug,
        String title,
        String bannerUrl,
        String content,
        String language,
        String status,
        Integer estimatedReading,
        OffsetDateTime lastViewedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<AuthorResponse> authors,
		List<TagResponse> tags,
		List<ProjectResponse> projects,
		Long viewCount,
		Long loveCount,
		Long celebrateCount,
		Long geniusCount,
		Long helpCount,
		Long reactionCount
) {}
