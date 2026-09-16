package dev.vitorpaulo.blog.model;

public record SubscriberQueryModel(
    String email,
    SubscriberStatus status,
    Language language,
    Frequency frequency
) {}
