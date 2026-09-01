package dev.vitorpaulo.blog.model.common;

import java.util.List;

public record PaginatedOutput<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
