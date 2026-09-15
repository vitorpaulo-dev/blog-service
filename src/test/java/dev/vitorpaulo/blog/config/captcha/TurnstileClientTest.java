package dev.vitorpaulo.blog.config.captcha;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurnstileClientTest {

	private static final String SECRET_KEY = "secret-key";

	@Mock private TurnstileFeignClient feignClient;

	@Mock private FeignException.InternalServerError serverError;

	@Mock private FeignException networkError;

	private TurnstileClient client;

	@BeforeEach
	void setUp() {
		client = new TurnstileClient(feignClient);
		ReflectionTestUtils.setField(client, "secretKey", SECRET_KEY);
	}

	@Test
	void verify_sendsSecretAndToken_returnsTrue() {
		when(feignClient.verifySite(any())).thenReturn(new TurnstileFeignClient.TurnstileVerificationResponse(true));

		assertTrue(client.verify("token"));

		final var captor = ArgumentCaptor.forClass(TurnstileFeignClient.TurnstileVerificationRequest.class);
		verify(feignClient).verifySite(captor.capture());

		final var request = captor.getValue();
		assertEquals(SECRET_KEY, request.secret());
		assertEquals("token", request.response());
	}

	@Test
	void verify_successResponse_returnsTrue() {
		when(feignClient.verifySite(any())).thenReturn(new TurnstileFeignClient.TurnstileVerificationResponse(true));

		assertTrue(client.verify("token"));
	}

	@Test
	void verify_failedResponse_returnsFalse() {
		when(feignClient.verifySite(any())).thenReturn(new TurnstileFeignClient.TurnstileVerificationResponse(false));

		assertFalse(client.verify("token"));
	}

	@Test
	void verify_nullResponse_returnsFalse() {
		when(feignClient.verifySite(any())).thenReturn(null);

		assertFalse(client.verify("token"));
	}

	@Test
	void verify_serverError_returnsFalse() {
		when(feignClient.verifySite(any())).thenThrow(serverError);

		assertFalse(client.verify("token"));
	}

	@Test
	void verify_networkError_returnsFalse() {
		when(feignClient.verifySite(any())).thenThrow(networkError);

		assertFalse(client.verify("token"));
	}
}
