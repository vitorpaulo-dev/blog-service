package dev.vitorpaulo.blog.input.response;

public record DashboardStatsResponse(
    long totalPosts,
    long publishedPosts,
    long draftPosts,
    long totalProjects,
    long publishedProjects,
    long draftProjects,
    long totalViews,
    long totalReactions,
    long totalSubscribers,
    long activeSubscribers
) {}
