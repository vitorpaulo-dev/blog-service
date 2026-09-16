package dev.vitorpaulo.blog.input.request;

public record ResendWebhookDataRequest(
    String email,
    Boolean unsubscribed
) {}
