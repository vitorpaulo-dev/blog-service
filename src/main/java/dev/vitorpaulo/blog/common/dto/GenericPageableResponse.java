package dev.vitorpaulo.blog.common.dto;

import java.util.List;

public record GenericPageableResponse<T>(
        List<T> content,
		Integer page,
		Integer size,
        Integer totalPages,
        Long totalElements
) {}
