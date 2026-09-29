package dev.vitorpaulo.blog.config.captcha;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurnstileClientTest {

	@Mock private RestClient restClient;
	@Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
	@Mock private RestClient.RequestBodySpec requestBodySpec;
	@Mock private RestClient.ResponseSpec responseSpec;

	@InjectMocks
	private TurnstileClient turnstileClient;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(turnstileClient, "secret", "test-secret");
	}

	@Test
	void verify_successResponse_returnsTrueWithSecretAndToken() {
		stubRequestChain();
		when(responseSpec.body(TurnstileClient.TurnstileVerificationResponse.class))
			.thenReturn(new TurnstileClient.TurnstileVerificationResponse(true));

		assertTrue(turnstileClient.verify("token-123"));

		var captor = ArgumentCaptor.forClass(TurnstileClient.TurnstileVerificationRequest.class);
		verify(requestBodySpec).body(captor.capture());
		assertEquals("test-secret", captor.getValue().secret());
		assertEquals("token-123", captor.getValue().response());
	}

	@Test
	void verify_rejectedResponse_returnsFalse() {
		stubRequestChain();
		when(responseSpec.body(TurnstileClient.TurnstileVerificationResponse.class))
			.thenReturn(new TurnstileClient.TurnstileVerificationResponse(false));

		assertFalse(turnstileClient.verify("token-123"));
	}

	@Test
	void verify_nullBody_returnsFalse() {
		stubRequestChain();
		when(responseSpec.body(TurnstileClient.TurnstileVerificationResponse.class)).thenReturn(null);

		assertFalse(turnstileClient.verify("token-123"));
	}

	@Test
	void verify_transportFailure_failsClosed() {
		when(restClient.post()).thenThrow(new ResourceAccessException("connection refused"));

		assertFalse(turnstileClient.verify("token-123"));
	}

	private void stubRequestChain() {
		when(restClient.post()).thenReturn(requestBodyUriSpec);
		when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
		when(requestBodySpec.body(any(Object.class))).thenReturn(requestBodySpec);
		when(requestBodySpec.retrieve()).thenReturn(responseSpec);
	}
}
