package dev.vitorpaulo.blog.usecase.newsletter;

import dev.vitorpaulo.blog.model.Frequency;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.SubscribeResultModel;
import dev.vitorpaulo.blog.model.SubscriberModel;
import dev.vitorpaulo.blog.model.SubscriberStatus;
import dev.vitorpaulo.blog.output.resend.ResendOutput;
import dev.vitorpaulo.blog.output.subscriber.SubscriberOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscribeUseCaseTest {

    private static final String EMAIL = "reader@example.com";
    private static final String CONTACT_ID = "resend-contact-1";
    private static final String EXISTING_CONTACT_ID = "resend-contact-0";
    private static final Frequency FREQUENCY = Frequency.EVERY_POST;
    private static final Language LANGUAGE = Language.ENGLISH;

    @Mock private SubscriberOutput subscriberOutput;
    @Mock private ResendOutput resendOutput;

    @InjectMocks
    private SubscribeUseCase subscribeUseCase;

    @Test
    void execute_alreadyActive_returnsExistingWithoutResendSync() {
        final var existing = new SubscriberModel(null, EMAIL, SubscriberStatus.ACTIVE, LANGUAGE, FREQUENCY, null, null);
        when(subscriberOutput.findByEmail(EMAIL)).thenReturn(Optional.of(existing));

        final var result = subscribeUseCase.execute(input());

        assertFalse(result.created());
        assertSame(existing, result.subscriber());
        verify(resendOutput, never()).createContact(anyString(), any(), any());
        verify(subscriberOutput, never()).update(any());
    }

    @Test
    void execute_reactivationWithoutContactId_createsNewContact() {
        final var existing = new SubscriberModel(idFixture(), EMAIL, SubscriberStatus.UNSUBSCRIBED, LANGUAGE, FREQUENCY, null, null);
        final var reactivated = new SubscriberModel(existing.id(), EMAIL, SubscriberStatus.ACTIVE, LANGUAGE, FREQUENCY, CONTACT_ID, null);

        when(subscriberOutput.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        when(resendOutput.createContact(EMAIL, FREQUENCY, LANGUAGE)).thenReturn(CONTACT_ID);
        when(subscriberOutput.update(reactivated)).thenReturn(reactivated);

        final var result = subscribeUseCase.execute(input());

        assertTrue(result.created());
        assertEquals(SubscriberStatus.ACTIVE, result.subscriber().status());
        assertEquals(CONTACT_ID, result.subscriber().resendContactId());
        verify(resendOutput, never()).reactivateContact(anyString(), any(), any());
    }

    @Test
    void execute_reactivationWithKnownContactId_patchesExistingContact() {
        final var existing = new SubscriberModel(idFixture(), EMAIL, SubscriberStatus.BOUNCED, LANGUAGE, FREQUENCY, EXISTING_CONTACT_ID, null);
        final var reactivated = new SubscriberModel(existing.id(), EMAIL, SubscriberStatus.ACTIVE, LANGUAGE, FREQUENCY, EXISTING_CONTACT_ID, null);

        when(subscriberOutput.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        when(subscriberOutput.update(reactivated)).thenReturn(reactivated);

        final var result = subscribeUseCase.execute(input());

        assertTrue(result.created());
        assertEquals(SubscriberStatus.ACTIVE, result.subscriber().status());
        assertEquals(EXISTING_CONTACT_ID, result.subscriber().resendContactId());
        verify(resendOutput).reactivateContact(EXISTING_CONTACT_ID, FREQUENCY, LANGUAGE);
        verify(resendOutput, never()).createContact(anyString(), any(), any());
    }

    @Test
    void execute_newSubscriber_savesAndStoresResendContactId() {
        final var saved = new SubscriberModel(idFixture(), EMAIL, SubscriberStatus.ACTIVE, LANGUAGE, FREQUENCY, null, null);
        final var withContact = new SubscriberModel(saved.id(), EMAIL, SubscriberStatus.ACTIVE, LANGUAGE, FREQUENCY, CONTACT_ID, null);

        when(subscriberOutput.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(subscriberOutput.save(any(SubscriberModel.class))).thenReturn(saved);
        when(resendOutput.createContact(EMAIL, FREQUENCY, LANGUAGE)).thenReturn(CONTACT_ID);
        when(subscriberOutput.update(withContact)).thenReturn(withContact);

        final var result = subscribeUseCase.execute(input());

        assertTrue(result.created());
        assertEquals(CONTACT_ID, result.subscriber().resendContactId());
        verify(subscriberOutput).save(any(SubscriberModel.class));
    }

    @Test
    void execute_resendFailure_propagatesWithoutPersistingContact() {
        when(subscriberOutput.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(subscriberOutput.save(any(SubscriberModel.class)))
            .thenReturn(new SubscriberModel(idFixture(), EMAIL, SubscriberStatus.ACTIVE, LANGUAGE, FREQUENCY, null, null));
        when(resendOutput.createContact(anyString(), any(), any())).thenThrow(new IllegalStateException("resend down"));

        assertThrows(IllegalStateException.class, () -> subscribeUseCase.execute(input()));

        verify(subscriberOutput, never()).update(any());
    }

    @Test
    void execute_resendFailureDuringReactivateWithKnownContactId_propagates() {
        final var existing = new SubscriberModel(idFixture(), EMAIL, SubscriberStatus.UNSUBSCRIBED, LANGUAGE, FREQUENCY, EXISTING_CONTACT_ID, null);

        when(subscriberOutput.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        doThrow(new IllegalStateException("resend down")).when(resendOutput).reactivateContact(anyString(), any(), any());

        assertThrows(IllegalStateException.class, () -> subscribeUseCase.execute(input()));

        verify(subscriberOutput, never()).update(any());
    }

    @Test
    void execute_resendFailureDuringReactivateWithoutContactId_propagates() {
        final var existing = new SubscriberModel(idFixture(), EMAIL, SubscriberStatus.UNSUBSCRIBED, LANGUAGE, FREQUENCY, null, null);

        when(subscriberOutput.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        when(resendOutput.createContact(anyString(), any(), any())).thenThrow(new IllegalStateException("resend down"));

        assertThrows(IllegalStateException.class, () -> subscribeUseCase.execute(input()));

        verify(subscriberOutput, never()).update(any());
    }

    private SubscriberModel input() {
        return new SubscriberModel(null, EMAIL, SubscriberStatus.ACTIVE, LANGUAGE, FREQUENCY, null, null);
    }

    private UUID idFixture() {
        return UUID.randomUUID();
    }
}
