package dev.vitorpaulo.blog.input.mapper;

import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.input.request.CreateTagRequest;
import dev.vitorpaulo.blog.input.request.TagContentRequest;
import dev.vitorpaulo.blog.input.request.TagQueryRequest;
import dev.vitorpaulo.blog.input.request.UpdateTagRequest;
import dev.vitorpaulo.blog.input.response.TagContentResponse;
import dev.vitorpaulo.blog.input.response.TagResponse;
import dev.vitorpaulo.blog.model.TagContentModel;
import dev.vitorpaulo.blog.model.TagModel;
import dev.vitorpaulo.blog.model.TagQueryModel;
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
public interface TagInputMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "slug", ignore = true)
	TagModel toModel(CreateTagRequest request);

	@Mapping(target = "slug", ignore = true)
	TagModel toModel(UpdateTagRequest request, UUID id);

	TagContentModel toContentModel(TagContentRequest request);

	TagResponse toResponse(TagModel model);

	GenericPageableResponse<TagResponse> toPageableResponse(PaginatedOutput<TagModel> result);

	PaginatedInput<TagQueryModel> toPageableInput(GenericPageableRequest<TagQueryRequest> request);
}