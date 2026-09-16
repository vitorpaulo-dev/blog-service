package dev.vitorpaulo.blog.input.response;

import dev.vitorpaulo.blog.model.Frequency;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.SubscriberStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SubscriberResponse(
    UUID id,
    String email,
    SubscriberStatus status,
    Language language,
    Frequency frequency,
    OffsetDateTime createdAt
) {}
