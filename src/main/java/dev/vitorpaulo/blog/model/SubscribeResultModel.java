package dev.vitorpaulo.blog.model;

public record SubscribeResultModel(
    boolean created,
    SubscriberModel subscriber
) {}
