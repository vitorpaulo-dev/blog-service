package dev.vitorpaulo.blog.config.exception.infrastructure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {

	FIELD_VALIDATION;
}
