package dev.vitorpaulo.blog.config.captcha;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TurnstileClient {

    private final TurnstileFeignClient turnstileFeignClient;

    private final String secretKey;

    public TurnstileClient(
            final TurnstileFeignClient turnstileFeignClient,
            @Value("${turnstile.secret-key}") final String secretKey
    ) {
        this.turnstileFeignClient = turnstileFeignClient;
        this.secretKey = secretKey;
    }

    public boolean verify(String token) {
        try {
            final var response = turnstileFeignClient.verifySite(
                    new TurnstileFeignClient.TurnstileVerificationRequest(secretKey, token)
            );

            return response != null && response.success();
        } catch (final FeignException exception) {
            return false;
        }
    }
}
