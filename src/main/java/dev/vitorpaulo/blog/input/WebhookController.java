package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.input.request.ResendWebhookRequest;
import dev.vitorpaulo.blog.model.ResendWebhookEventModel;
import dev.vitorpaulo.blog.usecase.newsletter.ProcessResendWebhookUseCase;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookSignatureVerifier webhookSignatureVerifier;
    private final ProcessResendWebhookUseCase processResendWebhookUseCase;
    private final ObjectMapper objectMapper;

    @PostMapping("/resend")
    public void resend(
        @RequestBody String body,
        @RequestHeader(value = "webhook-id", required = false) String webhookId,
        @RequestHeader(value = "webhook-timestamp", required = false) String webhookTimestamp,
        @RequestHeader(value = "webhook-signature", required = false) String webhookSignature
    ) {
        webhookSignatureVerifier.verify(body, webhookId, webhookTimestamp, webhookSignature);

        final var request = objectMapper.readValue(body, ResendWebhookRequest.class);
        processResendWebhookUseCase.execute(new ResendWebhookEventModel(
            request.type(),
            request.data() != null ? request.data().email() : null,
            request.data() != null ? request.data().unsubscribed() : null
        ));
    }
}
