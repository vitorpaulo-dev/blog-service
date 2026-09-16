package dev.vitorpaulo.blog.client.resend;

import java.util.List;

public record ResendCreateContactRequest(
    String email,
    boolean unsubscribed,
    List<ResendTopicSubscriptionRequest> topics
) {}
