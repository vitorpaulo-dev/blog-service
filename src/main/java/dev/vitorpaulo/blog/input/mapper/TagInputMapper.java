package dev.vitorpaulo.blog.input.mapper;

import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.input.request.CreateTagRequest;
import dev.vitorpaulo.blog.input.request.TagContentRequest;
import dev.vitorpaulo.blog.input.request.TagQueryRequest;
import dev.vitorpaulo.blog.input.request.UpdateTagRequest;
import dev.vitorpaulo.blog.input.response.TagContentResponse;
import dev.vitorpaulo.blog.input.response.TagResponse;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.TagContentModel;
import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.model.TagQueryModel;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Sort;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TagInputMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    TagModel toModel(CreateTagRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "slug", ignore = true)
    TagModel toModel(UpdateTagRequest request, UUID id);

    TagContentModel toContentModel(TagContentRequest request);

    default TagResponse toResponse(TagModel model) {
        if (model == null) return null;
        return new TagResponse(
            model.id(),
            model.slug(),
            toContentResponseMap(model.translations())
        );
    }

    default TagContentResponse toContentResponse(TagContentModel model) {
        if (model == null) return null;
        return new TagContentResponse(model.name());
    }

    default Map<Language, TagContentResponse> toContentResponseMap(Map<Language, TagContentModel> translations) {
        if (translations == null) return Map.of();
        return translations.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> toContentResponse(e.getValue())
        ));
    }

    default GenericPageableResponse<TagResponse> toPageableResponse(PaginatedOutput<TagModel> result) {
        if (result == null) return null;
        return new GenericPageableResponse<>(
            result.content().stream().map(this::toResponse).toList(),
            result.page(),
            result.size(),
            result.totalPages(),
            result.totalElements()
        );
    }

    default PaginatedInput<TagQueryModel> toPageableInput(@Valid GenericPageableRequest<TagQueryRequest> request) {
        if (request == null) return null;
        final var query = request.query();
        final var queryModel = query != null
            ? new TagQueryModel(query.name(), query.language())
            : new TagQueryModel(null, null);
        return new PaginatedInput<>(
            queryModel,
            request.page() != null ? request.page() : 0,
            request.size() != null ? request.size() : 10,
            request.sort(),
            request.direction() != null ? request.direction() : Sort.Direction.DESC
        );
    }
}
