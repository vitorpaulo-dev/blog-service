package dev.vitorpaulo.blog.config.exception.infrastructure;

import java.time.LocalDateTime;
import java.util.Map;

public record BusinessErrorResponse(
	ExceptionCode code,
	LocalDateTime timestamp,
	Map<String, Object> details
) {}