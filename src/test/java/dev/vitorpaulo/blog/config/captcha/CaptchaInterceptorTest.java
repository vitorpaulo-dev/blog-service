package dev.vitorpaulo.blog.config.captcha;

import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaptchaInterceptorTest {

	@Mock private TurnstileClient turnstileClient;

	@Mock private HandlerMethod annotatedMethod;

	@Mock private HandlerMethod unannotatedMethod;

	@InjectMocks
	private CaptchaInterceptor interceptor;

	private final HttpServletResponse response = new MockHttpServletResponse();

	@BeforeEach
	void setUp() {
		lenient().when(annotatedMethod.hasMethodAnnotation(ValidateCaptcha.class)).thenReturn(true);

		lenient().when(unannotatedMethod.hasMethodAnnotation(ValidateCaptcha.class)).thenReturn(false);
	}

	@Test
	void nonHandlerMethod_passesThrough() {
		assertTrue(interceptor.preHandle(request(), response, new Object()));
	}

	@Test
	void unannotatedMethod_passesThroughWithoutVerify() {
		assertTrue(interceptor.preHandle(request(), response, unannotatedMethod));

		verify(turnstileClient, never()).verify("token");
	}

	@Test
	void annotatedMethodWithoutToken_throwsForbidden() {
		final var exception = assertThrows(BusinessException.class,
				() -> interceptor.preHandle(request(), response, annotatedMethod));

		assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
		assertEquals(ExceptionCode.CAPTCHA_VALIDATION_FAILED, exception.getCode());
	}

	@Test
	void annotatedMethodWithFailedVerification_throwsForbidden() {
		final var request = request();
		request.addHeader("X-Captcha-Token", "token");
		when(turnstileClient.verify("token")).thenReturn(false);

		final var exception = assertThrows(BusinessException.class,
				() -> interceptor.preHandle(request, response, annotatedMethod));

		assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
		assertEquals(ExceptionCode.CAPTCHA_VALIDATION_FAILED, exception.getCode());
	}

	@Test
	void annotatedMethodWithValidToken_passesThrough() {
		final var request = request();
		request.addHeader("X-Captcha-Token", "token");
		when(turnstileClient.verify("token")).thenReturn(true);

		assertTrue(interceptor.preHandle(request, response, annotatedMethod));
	}

	private MockHttpServletRequest request() {
		return new MockHttpServletRequest();
	}
}
