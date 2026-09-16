package dev.vitorpaulo.blog.usecase.newsletter;

import dev.vitorpaulo.blog.model.ResendWebhookEventModel;
import dev.vitorpaulo.blog.model.SubscriberModel;
import dev.vitorpaulo.blog.model.SubscriberStatus;
import dev.vitorpaulo.blog.output.subscriber.SubscriberOutput;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProcessResendWebhookUseCase {

    private static final String CONTACT_UPDATED = "contact.updated";

    private static final Map<String, SubscriberStatus> STATUS_BY_EVENT = Map.of(
        CONTACT_UPDATED, SubscriberStatus.UNSUBSCRIBED,
        "contact.deleted", SubscriberStatus.UNSUBSCRIBED,
        "email.complained", SubscriberStatus.UNSUBSCRIBED,
        "email.bounced", SubscriberStatus.BOUNCED
    );

    private final SubscriberOutput subscriberOutput;

    public void execute(ResendWebhookEventModel event) {
        final var status = STATUS_BY_EVENT.get(event.type());
        if (status == null) {
            return;
        }

        if (CONTACT_UPDATED.equals(event.type()) && !Boolean.TRUE.equals(event.unsubscribed())) {
            return;
        }

        if (StringUtils.isBlank(event.email())) {
            return;
        }

        subscriberOutput.findByEmail(event.email()).ifPresent(subscriber ->
            subscriberOutput.update(new SubscriberModel(
                subscriber.id(),
                subscriber.email(),
                status,
                subscriber.language(),
                subscriber.frequency(),
                subscriber.resendContactId(),
                subscriber.createdAt()
            ))
        );
    }
}
