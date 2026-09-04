package dev.vitorpaulo.blog.common.exception;

import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class InternalException extends BusinessException {

	public InternalException() {
		super(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public InternalException(ExceptionCode code) {
		super(HttpStatus.INTERNAL_SERVER_ERROR, code, null);
	}

	public InternalException(ExceptionCode code, Map<String, Object> details) {
		super(HttpStatus.INTERNAL_SERVER_ERROR, code, details);
	}
}
