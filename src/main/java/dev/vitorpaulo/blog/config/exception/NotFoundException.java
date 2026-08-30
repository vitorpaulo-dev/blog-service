package dev.vitorpaulo.blog.config.exception;

import dev.vitorpaulo.blog.config.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.config.exception.infrastructure.ExceptionCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class NotFoundException extends BusinessException {

	public NotFoundException() {
		super(HttpStatus.NOT_FOUND);
	}

	public NotFoundException(ExceptionCode code) {
		super(HttpStatus.NOT_FOUND, code, null);
	}

	public NotFoundException(ExceptionCode code, Map<String, Object> details) {
		super(HttpStatus.NOT_FOUND, code, details);
	}
}
