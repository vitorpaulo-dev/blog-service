package dev.vitorpaulo.blog.common.exception.infrastructure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {

	FIELD_VALIDATION,
	POST_NOT_FOUND,
	POST_SLUG_NOT_FOUND,
	FORBIDDEN,
	UNAUTHORIZED,
	INVALID_PAGINATION,
	INVALID_SORT,
	AUTHOR_NOT_FOUND,
	TAG_NOT_FOUND,
	PROJECT_NOT_FOUND,
	SLUG_CONFLICT;
}
