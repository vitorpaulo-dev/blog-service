package dev.vitorpaulo.blog.output.resend;

import dev.vitorpaulo.blog.client.resend.ResendContactResponse;
import dev.vitorpaulo.blog.client.resend.ResendCreateContactRequest;
import dev.vitorpaulo.blog.client.resend.ResendFeignClient;
import dev.vitorpaulo.blog.client.resend.ResendTopicSubscriptionRequest;
import dev.vitorpaulo.blog.client.resend.ResendUpdateContactRequest;
import dev.vitorpaulo.blog.model.Frequency;
import dev.vitorpaulo.blog.model.Language;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ResendOutput {

    private static final String OPT_IN = "opt_in";
    private static final String OPT_OUT = "opt_out";

    @Value("${resend.topics.every-post}")
    private String everyPostTopicId;

    @Value("${resend.topics.monthly-digest}")
    private String monthlyDigestTopicId;

    @Value("${resend.topics.english}")
    private String englishTopicId;

    @Value("${resend.topics.portuguese}")
    private String portugueseTopicId;

    private final ResendFeignClient resendFeignClient;

    public String createContact(String email, Frequency frequency, Language language) {
        final var response = resendFeignClient.createContact(new ResendCreateContactRequest(
            email,
            false,
            topics(frequency, language)
        ));

        return response.id();
    }

    public void updateContactUnsubscribed(String contactId, boolean unsubscribed) {
        resendFeignClient.updateContact(contactId, new ResendUpdateContactRequest(unsubscribed));
    }

    public void updateContactTopics(String contactId, Frequency frequency, Language language) {
        resendFeignClient.updateContactTopics(contactId, topics(frequency, language));
    }

    public void reactivateContact(String contactId, Frequency frequency, Language language) {
        updateContactUnsubscribed(contactId, false);
        updateContactTopics(contactId, frequency, language);
    }

    private List<ResendTopicSubscriptionRequest> topics(Frequency frequency, Language language) {
        return List.of(
            new ResendTopicSubscriptionRequest(resolveFrequencyTopicId(frequency), OPT_IN),
            new ResendTopicSubscriptionRequest(resolveLanguageTopicId(language), OPT_IN)
        );
    }

    private String resolveFrequencyTopicId(Frequency frequency) {
        return frequency == Frequency.EVERY_POST ? everyPostTopicId : monthlyDigestTopicId;
    }

    private String resolveLanguageTopicId(Language language) {
        return language == Language.ENGLISH ? englishTopicId : portugueseTopicId;
    }
}
