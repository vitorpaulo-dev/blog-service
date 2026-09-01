package dev.vitorpaulo.blog.model.common;

import org.springframework.data.domain.Sort;

public record PaginatedInput<T>(
    T query,
    int page,
    int size,
	String sort,
	Sort.Direction direction
) {}
