package dev.vitorpaulo.blog.usecase.newsletter;

import dev.vitorpaulo.blog.common.exception.NotFoundException;
import dev.vitorpaulo.blog.model.SubscriberModel;
import dev.vitorpaulo.blog.model.SubscriberStatus;
import dev.vitorpaulo.blog.output.resend.ResendOutput;
import dev.vitorpaulo.blog.output.subscriber.SubscriberOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUnsubscribeUseCaseTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String CONTACT_ID = "resend-contact-1";

    @Mock private SubscriberOutput subscriberOutput;
    @Mock private ResendOutput resendOutput;

    @InjectMocks
    private AdminUnsubscribeUseCase adminUnsubscribeUseCase;

    @Test
    void execute_unknownId_propagatesNotFoundException() {
        when(subscriberOutput.findById(ID)).thenThrow(new NotFoundException());

        assertThrows(NotFoundException.class, () -> adminUnsubscribeUseCase.execute(ID));

        verify(resendOutput, never()).updateContactUnsubscribed(anyString(), org.mockito.ArgumentMatchers.anyBoolean());
    }

    @Test
    void execute_knownId_marksUnsubscribedAndSyncsResend() {
        final var subscriber = new SubscriberModel(ID, "reader@example.com", SubscriberStatus.ACTIVE, null, null, CONTACT_ID, null);
        final var unsubscribed = new SubscriberModel(ID, "reader@example.com", SubscriberStatus.UNSUBSCRIBED, null, null, CONTACT_ID, null);

        when(subscriberOutput.findById(ID)).thenReturn(subscriber);
        when(subscriberOutput.update(unsubscribed)).thenReturn(unsubscribed);

        final var result = adminUnsubscribeUseCase.execute(ID);

        assertEquals(SubscriberStatus.UNSUBSCRIBED, result.status());
        verify(resendOutput).updateContactUnsubscribed(CONTACT_ID, true);
    }

    @Test
    void execute_idempotent_alreadyUnsubscribedStillReturnsModel() {
        final var subscriber = new SubscriberModel(ID, "reader@example.com", SubscriberStatus.UNSUBSCRIBED, null, null, CONTACT_ID, null);

        when(subscriberOutput.findById(ID)).thenReturn(subscriber);
        when(subscriberOutput.update(org.mockito.ArgumentMatchers.any(SubscriberModel.class))).thenReturn(subscriber);

        final var result = adminUnsubscribeUseCase.execute(ID);

        assertEquals(SubscriberStatus.UNSUBSCRIBED, result.status());
    }

    @Test
    void execute_resendFailure_doesNotPropagate() {
        final var subscriber = new SubscriberModel(ID, "reader@example.com", SubscriberStatus.ACTIVE, null, null, CONTACT_ID, null);
        final var unsubscribed = new SubscriberModel(ID, "reader@example.com", SubscriberStatus.UNSUBSCRIBED, null, null, CONTACT_ID, null);

        when(subscriberOutput.findById(ID)).thenReturn(subscriber);
        when(subscriberOutput.update(org.mockito.ArgumentMatchers.any(SubscriberModel.class))).thenReturn(unsubscribed);
        doThrow(new IllegalStateException("resend down")).when(resendOutput).updateContactUnsubscribed(CONTACT_ID, true);

        final var result = adminUnsubscribeUseCase.execute(ID);

        assertEquals(SubscriberStatus.UNSUBSCRIBED, result.status());
    }

    @Test
    void execute_withoutResendContactId_skipsResendSync() {
        final var subscriber = new SubscriberModel(ID, "reader@example.com", SubscriberStatus.ACTIVE, null, null, null, null);

        when(subscriberOutput.findById(ID)).thenReturn(subscriber);
        when(subscriberOutput.update(org.mockito.ArgumentMatchers.any(SubscriberModel.class))).thenReturn(subscriber);

        adminUnsubscribeUseCase.execute(ID);

        verify(resendOutput, never()).updateContactUnsubscribed(anyString(), org.mockito.ArgumentMatchers.anyBoolean());
    }
}
