package dev.vitorpaulo.blog.output.mapper;

import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.domain.PostEntity;
import dev.vitorpaulo.blog.input.request.CreatePostRequest;
import dev.vitorpaulo.blog.input.request.PostQueryRequest;
import dev.vitorpaulo.blog.input.request.UpdatePostRequest;
import dev.vitorpaulo.blog.input.response.PostResponse;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface PostOutputMapper {

    PostModel toModel(PostEntity entity);

    PostEntity toEntity(PostModel enriched, Long estimatedReading, List<AuthorModel> authors, List<TagModel> tags, List<ProjectModel> projects);

	void updateEntity(PostModel source, @MappingTarget PostEntity target);


	PostResponse toResponse(PostModel model);

    PostModel toModel(CreatePostRequest request);

    PostModel toModel(UpdatePostRequest request, UUID id);

	GenericPageableResponse<PostResponse> toPageableResponse(PaginatedOutput<PostModel> result);

	PaginatedInput<PostQueryModel> toPageableInput(@Valid GenericPageableRequest<PostQueryRequest> request);
}
