package dev.vitorpaulo.blog.model;

public record DashboardStatsModel(
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
