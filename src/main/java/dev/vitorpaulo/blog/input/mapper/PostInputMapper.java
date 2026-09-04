package dev.vitorpaulo.blog.input.mapper;

import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.input.request.CreatePostRequest;
import dev.vitorpaulo.blog.input.request.PostContentRequest;
import dev.vitorpaulo.blog.input.request.PostQueryRequest;
import dev.vitorpaulo.blog.input.request.UpdatePostRequest;
import dev.vitorpaulo.blog.input.response.AuthorContentResponse;
import dev.vitorpaulo.blog.input.response.PostContentResponse;
import dev.vitorpaulo.blog.input.response.PostResponse;
import dev.vitorpaulo.blog.input.response.ProjectContentResponse;
import dev.vitorpaulo.blog.input.response.TagContentResponse;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PostInputMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "estimatedReading", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "authors", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "loveCount", ignore = true)
    @Mapping(target = "celebrateCount", ignore = true)
    @Mapping(target = "geniusCount", ignore = true)
    @Mapping(target = "helpCount", ignore = true)
    @Mapping(target = "reactionCount", ignore = true)
    PostModel toModel(CreatePostRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "estimatedReading", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "authors", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "loveCount", ignore = true)
    @Mapping(target = "celebrateCount", ignore = true)
    @Mapping(target = "geniusCount", ignore = true)
    @Mapping(target = "helpCount", ignore = true)
    @Mapping(target = "reactionCount", ignore = true)
    PostModel toModel(UpdatePostRequest request, UUID id);

    PostContentModel toContentModel(PostContentRequest request);

    default PostResponse toResponse(PostModel post) {
        if (post == null) return null;
        return new PostResponse(
            post.id(),
            post.slug(),
            post.bannerUrl(),
            post.status() != null ? post.status().name() : null,
            post.estimatedReading(),
            post.createdAt(),
            post.updatedAt(),
            post.authors() != null ? post.authors().stream().map(this::toAuthorResponse).toList() : List.of(),
            post.tags() != null ? post.tags().stream().map(this::toTagResponse).toList() : List.of(),
            post.projects() != null ? post.projects().stream().map(this::toProjectResponse).toList() : List.of(),
            post.viewCount(),
            post.loveCount(),
            post.celebrateCount(),
            post.geniusCount(),
            post.helpCount(),
            post.reactionCount(),
            toContentResponseMap(post.translations())
        );
    }

    default PostContentResponse toContentResponse(PostContentModel model) {
        if (model == null) return null;
        return new PostContentResponse(model.title(), model.content());
    }

    default Map<Language, PostContentResponse> toContentResponseMap(Map<Language, PostContentModel> translations) {
        if (translations == null) return Map.of();
        return translations.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> toContentResponse(e.getValue())
        ));
    }

    default dev.vitorpaulo.blog.input.response.AuthorResponse toAuthorResponse(AuthorModel model) {
        if (model == null) return null;
        final var contentMap = model.translations() != null
            ? model.translations().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> new AuthorContentResponse(e.getValue().bio(), e.getValue().jobTitle())
            ))
            : Map.<Language, AuthorContentResponse>of();
        return new dev.vitorpaulo.blog.input.response.AuthorResponse(
            model.id(), model.slug(), model.name(), model.avatarUrl(), contentMap
        );
    }

    default dev.vitorpaulo.blog.input.response.TagResponse toTagResponse(TagModel model) {
        if (model == null) return null;
        final var contentMap = model.translations() != null
            ? model.translations().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> new TagContentResponse(e.getValue().name(), e.getValue().description())
            ))
            : Map.<Language, TagContentResponse>of();
        return new dev.vitorpaulo.blog.input.response.TagResponse(model.id(), model.slug(), contentMap);
    }

    default dev.vitorpaulo.blog.input.response.ProjectResponse toProjectResponse(ProjectModel model) {
        if (model == null) return null;
        final var contentMap = model.translations() != null
            ? model.translations().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> new ProjectContentResponse(e.getValue().title(), e.getValue().description())
            ))
            : Map.<Language, ProjectContentResponse>of();
        return new dev.vitorpaulo.blog.input.response.ProjectResponse(
            model.id(), model.slug(), model.logoUrl(), model.programmingLanguage(), contentMap
        );
    }

    default GenericPageableResponse<PostResponse> toPageableResponse(PaginatedOutput<PostModel> result) {
        if (result == null) return null;
        return new GenericPageableResponse<>(
            result.content().stream().map(this::toResponse).toList(),
            result.page(),
            result.size(),
            result.totalPages(),
            result.totalElements()
        );
    }

    default PaginatedInput<PostQueryModel> toPageableInput(@Valid GenericPageableRequest<PostQueryRequest> request) {
        if (request == null) return null;
        final var query = request.query();
        final var queryModel = query != null
            ? new PostQueryModel(query.query(), query.authorId(), query.language())
            : new PostQueryModel(null, null, null);
        return new PaginatedInput<>(
            queryModel,
            request.page() != null ? request.page() : 0,
            request.size() != null ? request.size() : 10,
            request.sort(),
            request.direction()
        );
    }
}
