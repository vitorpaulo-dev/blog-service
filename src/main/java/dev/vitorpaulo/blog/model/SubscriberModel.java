package dev.vitorpaulo.blog.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SubscriberModel(
    UUID id,
    String email,
    SubscriberStatus status,
    Language language,
    Frequency frequency,
    String resendContactId,
    OffsetDateTime createdAt
) {}
