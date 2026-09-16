package dev.vitorpaulo.blog.usecase.newsletter;

import dev.vitorpaulo.blog.model.ResendWebhookEventModel;
import dev.vitorpaulo.blog.model.SubscriberModel;
import dev.vitorpaulo.blog.model.SubscriberStatus;
import dev.vitorpaulo.blog.output.subscriber.SubscriberOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessResendWebhookUseCaseTest {

    private static final String EMAIL = "reader@example.com";

    @Mock private SubscriberOutput subscriberOutput;

    @InjectMocks
    private ProcessResendWebhookUseCase processResendWebhookUseCase;

    @ParameterizedTest
    @CsvSource({
        "contact.updated, true, UNSUBSCRIBED",
        "contact.deleted, null, UNSUBSCRIBED",
        "email.complained, null, UNSUBSCRIBED",
        "email.bounced, null, BOUNCED"
    })
    void execute_eventMapped_updatesSubscriberStatus(String type, String unsubscribed, String expectedStatus) {
        final var unsubscribedValue = "null".equals(unsubscribed) ? null : Boolean.valueOf(unsubscribed);
        final var subscriber = new SubscriberModel(UUID.randomUUID(), EMAIL, SubscriberStatus.ACTIVE, null, null, null, null);

        when(subscriberOutput.findByEmail(EMAIL)).thenReturn(Optional.of(subscriber));

        processResendWebhookUseCase.execute(new ResendWebhookEventModel(type, EMAIL, unsubscribedValue));

        final var captor = ArgumentCaptor.forClass(SubscriberModel.class);
        verify(subscriberOutput).update(captor.capture());
        assertEquals(SubscriberStatus.valueOf(expectedStatus), captor.getValue().status());
    }

    @Test
    void execute_contactUpdatedWithoutUnsubscribed_isIgnored() {
        processResendWebhookUseCase.execute(new ResendWebhookEventModel("contact.updated", EMAIL, false));

        verify(subscriberOutput, never()).findByEmail(any());
        verify(subscriberOutput, never()).update(any());
    }

    @Test
    void execute_unknownEventType_isIgnored() {
        processResendWebhookUseCase.execute(new ResendWebhookEventModel("unknown.event", EMAIL, true));

        verify(subscriberOutput, never()).findByEmail(any());
        verify(subscriberOutput, never()).update(any());
    }

    @Test
    void execute_missingEmail_isIgnored() {
        processResendWebhookUseCase.execute(new ResendWebhookEventModel("contact.deleted", null, null));

        verify(subscriberOutput, never()).findByEmail(any());
        verify(subscriberOutput, never()).update(any());
    }

    @Test
    void execute_unknownSubscriber_isIgnored() {
        when(subscriberOutput.findByEmail(EMAIL)).thenReturn(Optional.empty());

        processResendWebhookUseCase.execute(new ResendWebhookEventModel("contact.deleted", EMAIL, null));

        verify(subscriberOutput, never()).update(any());
    }
}
