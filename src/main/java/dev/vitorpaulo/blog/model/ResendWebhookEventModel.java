package dev.vitorpaulo.blog.model;

public record ResendWebhookEventModel(
    String type,
    String email,
    Boolean unsubscribed
) {}
