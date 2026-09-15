package dev.vitorpaulo.blog.config.captcha;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "cloudflare-turnstile", url = "https://challenges.cloudflare.com/turnstile/v0")
public interface TurnstileFeignClient {

    @PostMapping("/siteverify")
    TurnstileVerificationResponse verifySite(TurnstileVerificationRequest request);

    record TurnstileVerificationRequest(String secret, String response) {
    }

    record TurnstileVerificationResponse(boolean success) {
    }
}
