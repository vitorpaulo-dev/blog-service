package dev.vitorpaulo.blog.usecase.newsletter;

import dev.vitorpaulo.blog.model.SubscribeResultModel;
import dev.vitorpaulo.blog.model.SubscriberModel;
import dev.vitorpaulo.blog.model.SubscriberStatus;
import dev.vitorpaulo.blog.output.resend.ResendOutput;
import dev.vitorpaulo.blog.output.subscriber.SubscriberOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SubscribeUseCase {

    private final SubscriberOutput subscriberOutput;
    private final ResendOutput resendOutput;

    @Transactional
    public SubscribeResultModel execute(SubscriberModel input) {
        final var existing = subscriberOutput.findByEmail(input.email());

        if (existing.isPresent()) {
            return reactivate(existing.get(), input);
        }

        final var saved = subscriberOutput.save(new SubscriberModel(
            null,
            input.email(),
            SubscriberStatus.ACTIVE,
            input.language(),
            input.frequency(),
            null,
            null
        ));
        final var contactId = resendOutput.createContact(saved.email(), saved.frequency(), saved.language());

        return new SubscribeResultModel(true, storeResendContactId(saved, contactId));
    }

    private SubscribeResultModel reactivate(SubscriberModel subscriber, SubscriberModel input) {
        if (subscriber.status() == SubscriberStatus.ACTIVE) {
            return new SubscribeResultModel(false, subscriber);
        }

        final var contactId = resendContactId(subscriber, input);
        final var reactivated = new SubscriberModel(
            subscriber.id(),
            subscriber.email(),
            SubscriberStatus.ACTIVE,
            input.language(),
            input.frequency(),
            contactId,
            subscriber.createdAt()
        );

        return new SubscribeResultModel(true, subscriberOutput.update(reactivated));
    }

    private String resendContactId(SubscriberModel subscriber, SubscriberModel input) {
        final var contactId = subscriber.resendContactId();

        if (contactId != null) {
            resendOutput.reactivateContact(contactId, input.frequency(), input.language());
            return contactId;
        }

        return resendOutput.createContact(input.email(), input.frequency(), input.language());
    }

    private SubscriberModel storeResendContactId(SubscriberModel subscriber, String contactId) {
        return subscriberOutput.update(new SubscriberModel(
            subscriber.id(),
            subscriber.email(),
            subscriber.status(),
            subscriber.language(),
            subscriber.frequency(),
            contactId,
            subscriber.createdAt()
        ));
    }
}
