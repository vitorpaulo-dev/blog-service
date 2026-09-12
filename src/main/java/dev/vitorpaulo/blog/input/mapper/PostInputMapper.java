package dev.vitorpaulo.blog.input.mapper;

import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.input.request.CreatePostRequest;
import dev.vitorpaulo.blog.input.request.PostContentRequest;
import dev.vitorpaulo.blog.input.request.PostQueryRequest;
import dev.vitorpaulo.blog.input.request.UpdatePostRequest;
import dev.vitorpaulo.blog.input.response.AuthorResponse;
import dev.vitorpaulo.blog.input.response.PostContentResponse;
import dev.vitorpaulo.blog.input.response.PostResponse;
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
public interface PostInputMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "slug", ignore = true)
	@Mapping(target = "estimatedReading", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "authors", ignore = true)
	@Mapping(target = "tagIds", ignore = true)
	@Mapping(target = "projectIds", ignore = true)
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
	@Mapping(target = "tagIds", ignore = true)
	@Mapping(target = "projectIds", ignore = true)
	@Mapping(target = "viewCount", ignore = true)
	@Mapping(target = "loveCount", ignore = true)
	@Mapping(target = "celebrateCount", ignore = true)
	@Mapping(target = "geniusCount", ignore = true)
	@Mapping(target = "helpCount", ignore = true)
	@Mapping(target = "reactionCount", ignore = true)
	PostModel toModel(UpdatePostRequest request, UUID id);

	PostContentModel toContentModel(PostContentRequest request);

	PostResponse toResponse(PostModel post);

	GenericPageableResponse<PostResponse> toPageableResponse(PaginatedOutput<PostModel> result);

	PaginatedInput<PostQueryModel> toPageableInput(GenericPageableRequest<PostQueryRequest> request);
}