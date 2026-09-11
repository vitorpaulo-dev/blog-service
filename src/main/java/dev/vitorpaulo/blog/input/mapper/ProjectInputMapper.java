package dev.vitorpaulo.blog.input.mapper;

import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.input.request.CreateProjectRequest;
import dev.vitorpaulo.blog.input.request.ProjectContentRequest;
import dev.vitorpaulo.blog.input.request.ProjectQueryRequest;
import dev.vitorpaulo.blog.input.request.UpdateProjectRequest;
import dev.vitorpaulo.blog.input.response.AuthorContentResponse;
import dev.vitorpaulo.blog.input.response.ProjectContentResponse;
import dev.vitorpaulo.blog.input.response.ProjectResponse;
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
public interface ProjectInputMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "authors", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "loveCount", ignore = true)
    @Mapping(target = "celebrateCount", ignore = true)
    @Mapping(target = "geniusCount", ignore = true)
    @Mapping(target = "helpCount", ignore = true)
    @Mapping(target = "reactionCount", ignore = true)
    ProjectModel toModel(CreateProjectRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "authors", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "loveCount", ignore = true)
    @Mapping(target = "celebrateCount", ignore = true)
    @Mapping(target = "geniusCount", ignore = true)
    @Mapping(target = "helpCount", ignore = true)
    @Mapping(target = "reactionCount", ignore = true)
    ProjectModel toModel(UpdateProjectRequest request, UUID id);

    ProjectContentModel toContentModel(ProjectContentRequest request);

    default ProjectResponse toResponse(ProjectModel project) {
        if (project == null) return null;
        return new ProjectResponse(
            project.id(),
            project.slug(),
            project.logoUrl(),
            project.programmingLanguage(),
            project.bannerUrl(),
            project.githubUrl(),
            project.websiteUrl(),
            project.status() != null ? project.status().name() : null,
            project.createdAt(),
            project.updatedAt(),
            project.authors() != null ? project.authors().stream().map(this::toAuthorResponse).toList() : List.of(),
            project.viewCount(),
            project.loveCount(),
            project.celebrateCount(),
            project.geniusCount(),
            project.helpCount(),
            project.reactionCount(),
            toContentResponseMap(project.translations())
        );
    }

    default ProjectContentResponse toContentResponse(ProjectContentModel model) {
        if (model == null) return null;
        return new ProjectContentResponse(model.title(), model.description());
    }

    default Map<Language, ProjectContentResponse> toContentResponseMap(Map<Language, ProjectContentModel> translations) {
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

    default GenericPageableResponse<ProjectResponse> toPageableResponse(PaginatedOutput<ProjectModel> result) {
        if (result == null) return null;
        return new GenericPageableResponse<>(
            result.content().stream().map(this::toResponse).toList(),
            result.page(),
            result.size(),
            result.totalPages(),
            result.totalElements()
        );
    }

    default PaginatedInput<ProjectQueryModel> toPageableInput(@Valid GenericPageableRequest<ProjectQueryRequest> request) {
        if (request == null) return null;
        final var query = request.query();
        final var queryModel = query != null
            ? new ProjectQueryModel(query.query(), query.authorId(), query.language())
            : new ProjectQueryModel(null, null, null);
        return new PaginatedInput<>(
            queryModel,
            request.page() != null ? request.page() : 0,
            request.size() != null ? request.size() : 10,
            request.sort(),
            request.direction()
        );
    }
}
