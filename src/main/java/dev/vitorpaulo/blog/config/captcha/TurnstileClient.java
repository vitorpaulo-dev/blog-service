package dev.vitorpaulo.blog.config.captcha;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class TurnstileClient {

	private static final String VERIFY_URL =
		"https://challenges.cloudflare.com/turnstile/v0/siteverify";

	private final RestClient restClient;

	@Value("${turnstile.secret}")
	private String secret;

	public boolean verify(String token) {
		final var response = restClient.post()
			.uri(VERIFY_URL)
			.body(new TurnstileVerificationRequest(secret, token))
			.retrieve()
			.body(TurnstileVerificationResponse.class);

		return response != null && response.success();
	}

	private record TurnstileVerificationRequest(
		String secret,
		String response
	) {}

	private record TurnstileVerificationResponse(
		boolean success
	) {}
}