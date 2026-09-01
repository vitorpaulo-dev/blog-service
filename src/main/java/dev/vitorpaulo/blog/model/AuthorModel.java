package dev.vitorpaulo.blog.model;

import java.util.UUID;

public record AuthorModel(
        UUID id,
        String clerkUserId,
        String name,
        String avatarUrl,
        String jobTitle,
		String role
) {}
