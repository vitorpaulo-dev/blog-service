package dev.vitorpaulo.blog.input.mapper;

import dev.vitorpaulo.blog.input.request.PresignUploadRequest;
import dev.vitorpaulo.blog.input.response.PresignUploadResponse;
import dev.vitorpaulo.blog.model.upload.PresignUploadModel;
import dev.vitorpaulo.blog.model.upload.PresignUploadRequestModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UploadInputMapper {

	PresignUploadRequestModel toModel(PresignUploadRequest request);

	PresignUploadResponse toResponse(PresignUploadModel model);
}
