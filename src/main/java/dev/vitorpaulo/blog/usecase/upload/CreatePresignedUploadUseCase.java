package dev.vitorpaulo.blog.usecase.upload;

import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.model.upload.PresignUploadModel;
import dev.vitorpaulo.blog.model.upload.PresignUploadRequestModel;
import dev.vitorpaulo.blog.model.upload.UploadFolder;
import dev.vitorpaulo.blog.output.upload.StorageOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class CreatePresignedUploadUseCase {

	private final StorageOutput storageOutput;

	public PresignUploadModel execute(PresignUploadRequestModel request) {
		final var folder = Arrays.stream(UploadFolder.values())
			.filter(f -> f.getFolder().equals(request.folder()) && f.getSubfolder().equals(request.subfolder()))
			.findFirst()
			.orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, ExceptionCode.UPLOAD_FAILED, null));

		final var presigned = storageOutput.presignUpload(folder.getFolder(), folder.getSubfolder(), request.fileName());

		return new PresignUploadModel(presigned.key(), presigned.url());
	}
}
