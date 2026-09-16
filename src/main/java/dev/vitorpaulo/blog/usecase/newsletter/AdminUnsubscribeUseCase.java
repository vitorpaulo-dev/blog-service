package dev.vitorpaulo.blog.usecase.newsletter;

import dev.vitorpaulo.blog.model.SubscriberModel;
import dev.vitorpaulo.blog.model.SubscriberStatus;
import dev.vitorpaulo.blog.output.resend.ResendOutput;
import dev.vitorpaulo.blog.output.subscriber.SubscriberOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminUnsubscribeUseCase {

    private final SubscriberOutput subscriberOutput;
    private final ResendOutput resendOutput;

    @Transactional
    public SubscriberModel execute(UUID id) {
        final var subscriber = subscriberOutput.findById(id);
        final var result = subscriberOutput.update(new SubscriberModel(
            subscriber.id(),
            subscriber.email(),
            SubscriberStatus.UNSUBSCRIBED,
            subscriber.language(),
            subscriber.frequency(),
            subscriber.resendContactId(),
            subscriber.createdAt()
        ));

        if (subscriber.resendContactId() != null) {
            try {
                resendOutput.updateContactUnsubscribed(subscriber.resendContactId(), true);
            } catch (Exception exception) {
                log.warn("Resend unsubscribe failed for subscriber {}", id, exception);
            }
        }

        return result;
    }
}
