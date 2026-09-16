package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Component
public class WebhookSignatureVerifier {

    private static final String SECRET_PREFIX = "whsec_";
    private static final String SIGNATURE_VERSION_PREFIX = "v1,";

    @Value("${resend.webhook.secret}")
    private String secret;

    public void verify(String body, String webhookId, String webhookTimestamp, String webhookSignature) {
        if (webhookId == null || webhookTimestamp == null || webhookSignature == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST);
        }

        final var signedContent = webhookId + "." + webhookTimestamp + "." + body;
        final var expected = Base64.getEncoder().encodeToString(hmacSha256(signedContent));

        final var valid = Arrays.stream(webhookSignature.split(" "))
            .filter(part -> part.startsWith(SIGNATURE_VERSION_PREFIX))
            .anyMatch(part -> MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                part.substring(SIGNATURE_VERSION_PREFIX.length()).getBytes(StandardCharsets.UTF_8)
            ));

        if (!valid) {
            throw new BusinessException(HttpStatus.FORBIDDEN, ExceptionCode.WEBHOOK_SIGNATURE_INVALID, null);
        }
    }

    private byte[] hmacSha256(String content) {
        try {
            final var key = Base64.getDecoder().decode(secret.replaceFirst("^" + SECRET_PREFIX, ""));
            final var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));

            return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to verify webhook signature", exception);
        }
    }
}
