package dev.vitorpaulo.blog.config.captcha;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;

@Component
@RequiredArgsConstructor
public class CaptchaInterceptor implements HandlerInterceptor {

    private static final String CAPTCHA_TOKEN_HEADER = "X-Captcha-Token";

    private final TurnstileClient turnstileClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof final HandlerMethod handlerMethod)) {
            return true;
        }

        if (!handlerMethod.hasMethodAnnotation(ValidateCaptcha.class)) {
            return true;
        }

        final var token = request.getHeader(CAPTCHA_TOKEN_HEADER);
        if (StringUtils.isBlank(token) || !turnstileClient.verify(token)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, ExceptionCode.CAPTCHA_VALIDATION_FAILED, null);
        }

        return true;
    }
}
