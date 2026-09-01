package dev.vitorpaulo.blog.input.mapper;

import dev.vitorpaulo.blog.common.dto.GenericPageableRequest;
import dev.vitorpaulo.blog.common.dto.GenericPageableResponse;
import dev.vitorpaulo.blog.input.request.CreatePostRequest;
import dev.vitorpaulo.blog.input.request.PostQueryRequest;
import dev.vitorpaulo.blog.input.request.UpdatePostRequest;
import dev.vitorpaulo.blog.input.response.PostResponse;
import dev.vitorpaulo.blog.model.*;
import dev.vitorpaulo.blog.model.common.PaginatedInput;
import dev.vitorpaulo.blog.model.common.PaginatedOutput;
import jakarta.validation.Valid;
import org.mapstruct.Mapper;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface PostInputMapper {

    PostModel toModel(CreatePostRequest request);

    PostModel toModel(UpdatePostRequest request, UUID id);

	GenericPageableResponse<PostResponse> toPageableResponse(PaginatedOutput<PostModel> result);

	PaginatedInput<PostQueryModel> toPageableInput(@Valid GenericPageableRequest<PostQueryRequest> request);

	PostResponse toResponse(PostModel post);
}
