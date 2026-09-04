package dev.vitorpaulo.blog.common.exception.infrastructure;

import java.time.LocalDateTime;
import java.util.Map;

public record BusinessErrorResponse(
	ExceptionCode code,
	LocalDateTime timestamp,
	Map<String, Object> details
) {}