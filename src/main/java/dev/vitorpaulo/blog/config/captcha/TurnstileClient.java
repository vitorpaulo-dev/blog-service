package dev.vitorpaulo.blog.config.captcha;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TurnstileClient {

    private final TurnstileFeignClient turnstileFeignClient;

    @Value("${turnstile.secret-key}")
    private String secretKey;

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
