package dev.vitorpaulo.blog.config.captcha;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TurnstileClient {

	private static final String VERIFY_URL =
		"https://challenges.cloudflare.com/turnstile/v0/siteverify";

	private final RestClient restClient;

	@Value("${turnstile.secret-key}")
	private String secret;

	public boolean verify(String token) {
		try {
			final var response = restClient.post()
				.uri(VERIFY_URL)
				.body(new TurnstileVerificationRequest(secret, token))
				.retrieve()
				.body(TurnstileVerificationResponse.class);

			return response != null && response.success();
		} catch (RestClientException exception) {
			log.warn("Turnstile verification request failed, failing closed", exception);
			return false;
		}
	}

	record TurnstileVerificationRequest(
		String secret,
		String response
	) {}

	record TurnstileVerificationResponse(
		boolean success
	) {}
}
