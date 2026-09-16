package dev.vitorpaulo.blog.input.request;

public record ResendWebhookRequest(
    String type,
    ResendWebhookDataRequest data
) {}
