package dev.vitorpaulo.blog.common.dto;

import java.io.Serializable;
import java.util.List;
import org.springframework.data.domain.Page;

public record GenericPageableResponse<T>(
        List<T> content,
        Integer totalPages,
        Long totalElements
) implements Serializable {

    public GenericPageableResponse(List<T> content, Page<?> page) {
        this(content, page.getTotalPages(), page.getTotalElements());
    }
}
