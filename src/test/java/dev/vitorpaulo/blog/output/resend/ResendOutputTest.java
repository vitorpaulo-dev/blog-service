package dev.vitorpaulo.blog.output.resend;

import dev.vitorpaulo.blog.client.resend.ResendContactResponse;
import dev.vitorpaulo.blog.client.resend.ResendCreateContactRequest;
import dev.vitorpaulo.blog.client.resend.ResendFeignClient;
import dev.vitorpaulo.blog.client.resend.ResendTopicSubscriptionRequest;
import dev.vitorpaulo.blog.client.resend.ResendUpdateContactRequest;
import dev.vitorpaulo.blog.model.Frequency;
import dev.vitorpaulo.blog.model.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResendOutputTest {

    @Mock private ResendFeignClient resendFeignClient;

    private ResendOutput resendOutput;

    @BeforeEach
    void setUp() {
        resendOutput = new ResendOutput(resendFeignClient);
        ReflectionTestUtils.setField(resendOutput, "everyPostTopicId", "topic-every-post");
        ReflectionTestUtils.setField(resendOutput, "monthlyDigestTopicId", "topic-monthly-digest");
        ReflectionTestUtils.setField(resendOutput, "englishTopicId", "topic-english");
        ReflectionTestUtils.setField(resendOutput, "portugueseTopicId", "topic-portuguese");
    }

    @Test
    void createContact_sendsFrequencyAndLanguageTopicsOptIn() {
        when(resendFeignClient.createContact(any(ResendCreateContactRequest.class)))
            .thenReturn(new ResendContactResponse("contact-1"));

        final var contactId = resendOutput.createContact("reader@example.com", Frequency.EVERY_POST, Language.PORTUGUESE);

        assertEquals("contact-1", contactId);

        final var captor = ArgumentCaptor.forClass(ResendCreateContactRequest.class);
        verify(resendFeignClient).createContact(captor.capture());

        final var request = captor.getValue();
        assertEquals("reader@example.com", request.email());
        assertEquals(Boolean.FALSE, request.unsubscribed());
        assertEquals(List.of(
            new ResendTopicSubscriptionRequest("topic-every-post", "opt_in"),
            new ResendTopicSubscriptionRequest("topic-portuguese", "opt_in")
        ), request.topics());
    }

    @Test
    void createContact_resolvesMonthlyDigestAndEnglishTopics() {
        when(resendFeignClient.createContact(any(ResendCreateContactRequest.class)))
            .thenReturn(new ResendContactResponse("contact-2"));

        resendOutput.createContact("reader@example.com", Frequency.MONTHLY_DIGEST, Language.ENGLISH);

        final var captor = ArgumentCaptor.forClass(ResendCreateContactRequest.class);
        verify(resendFeignClient).createContact(captor.capture());

        assertEquals(List.of(
            new ResendTopicSubscriptionRequest("topic-monthly-digest", "opt_in"),
            new ResendTopicSubscriptionRequest("topic-english", "opt_in")
        ), captor.getValue().topics());
    }

    @Test
    void updateContactUnsubscribed_patchesUnsubscribedFlag() {
        resendOutput.updateContactUnsubscribed("contact-1", true);

        final var captor = ArgumentCaptor.forClass(ResendUpdateContactRequest.class);
        verify(resendFeignClient).updateContact(eq("contact-1"), captor.capture());
        assertEquals(Boolean.TRUE, captor.getValue().unsubscribed());
    }

    @Test
    void updateContactTopics_patchesTopicsArrayToDedicatedEndpoint() {
        resendOutput.updateContactTopics("contact-1", Frequency.MONTHLY_DIGEST, Language.ENGLISH);

        @SuppressWarnings("unchecked")
        final var captor = ArgumentCaptor.forClass((Class<List<ResendTopicSubscriptionRequest>>) (Class<?>) List.class);
        verify(resendFeignClient).updateContactTopics(eq("contact-1"), captor.capture());

        assertEquals(List.of(
            new ResendTopicSubscriptionRequest("topic-monthly-digest", "opt_in"),
            new ResendTopicSubscriptionRequest("topic-english", "opt_in")
        ), captor.getValue());
    }

    @Test
    void reactivateContact_clearsUnsubscribedAndReoptsTopics() {
        resendOutput.reactivateContact("contact-1", Frequency.EVERY_POST, Language.PORTUGUESE);

        final var unsubscribedCaptor = ArgumentCaptor.forClass(ResendUpdateContactRequest.class);
        verify(resendFeignClient).updateContact(eq("contact-1"), unsubscribedCaptor.capture());
        assertEquals(Boolean.FALSE, unsubscribedCaptor.getValue().unsubscribed());

        @SuppressWarnings("unchecked")
        final var topicsCaptor = ArgumentCaptor.forClass((Class<List<ResendTopicSubscriptionRequest>>) (Class<?>) List.class);
        verify(resendFeignClient).updateContactTopics(eq("contact-1"), topicsCaptor.capture());

        assertEquals(List.of(
            new ResendTopicSubscriptionRequest("topic-every-post", "opt_in"),
            new ResendTopicSubscriptionRequest("topic-portuguese", "opt_in")
        ), topicsCaptor.getValue());
    }

    @Test
    void createContact_responseWithoutEmailKeepsOwnEmailOnly() {
        when(resendFeignClient.createContact(any(ResendCreateContactRequest.class)))
            .thenReturn(new ResendContactResponse(null));

        assertNull(resendOutput.createContact("reader@example.com", Frequency.EVERY_POST, Language.ENGLISH));
    }
}
