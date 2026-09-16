package dev.vitorpaulo.blog.input.request;

import dev.vitorpaulo.blog.model.Frequency;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.SubscriberStatus;

public record SubscriberQueryRequest(
    String email,
    SubscriberStatus status,
    Language language,
    Frequency frequency
) {}
