package dev.vitorpaulo.blog.common.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Sort;

public record GenericPageableRequest<T>(
        T query,
        @Min(0) Integer page,
        @Min(1) @Max(50) Integer limit,
        String sort,
		Sort.Direction direction
) {}
