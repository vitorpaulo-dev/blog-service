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
import dev.vitorpaulo.blog.input.response.TagContentResponse;
import dev.vitorpaulo.blog.input.response.TagResponse;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import jakarta.validation.Valid;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface ProjectInputMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "slug", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "authors", ignore = true)
	@Mapping(target = "tags", ignore = true)
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
	@Mapping(target = "tags", ignore = true)
	@Mapping(target = "viewCount", ignore = true)
	@Mapping(target = "loveCount", ignore = true)
	@Mapping(target = "celebrateCount", ignore = true)
	@Mapping(target = "geniusCount", ignore = true)
	@Mapping(target = "helpCount", ignore = true)
	@Mapping(target = "reactionCount", ignore = true)
	ProjectModel toModel(UpdateProjectRequest request, UUID id);

	ProjectContentModel toContentModel(ProjectContentRequest request);

	ProjectResponse toResponse(ProjectModel project);

	GenericPageableResponse<ProjectResponse> toPageableResponse(PaginatedOutput<ProjectModel> result);

	PaginatedInput<ProjectQueryModel> toPageableInput(GenericPageableRequest<ProjectQueryRequest> request);
}