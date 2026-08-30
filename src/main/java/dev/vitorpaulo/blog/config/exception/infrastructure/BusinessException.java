package dev.vitorpaulo.blog.config.exception.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class BusinessException extends RuntimeException {
	private final HttpStatus status;
	private ExceptionCode code;
    private Map<String, Object> details;
}
