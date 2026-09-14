package dev.vitorpaulo.blog.usecase.upload;

import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.model.upload.PresignUploadModel;
import dev.vitorpaulo.blog.model.upload.PresignUploadRequestModel;
import dev.vitorpaulo.blog.model.upload.SignedUrlModel;
import dev.vitorpaulo.blog.output.upload.StorageOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePresignedUploadUseCaseTest {

	@InjectMocks
	private CreatePresignedUploadUseCase useCase;

	@Mock
	private StorageOutput storageOutput;

	@Test
	void validFolderAndSubfolder_presigns() {
		final var request = new PresignUploadRequestModel("post", "banner", "foto heye.PNG");
		when(storageOutput.presignUpload("post", "banner", "foto heye.PNG"))
			.thenReturn(new SignedUrlModel("post/banner/uuid.png", "https://r2/signed-put"));

		final var result = useCase.execute(request);

		assertEquals("post/banner/uuid.png", result.key());
		assertEquals("https://r2/signed-put", result.uploadUrl());
		verify(storageOutput).presignUpload(eq("post"), eq("banner"), eq("foto heye.PNG"));
	}

	@Test
	void projectContentFolderInvalidSubfolder_throwsBadRequest() {
		// valid folder name but subfolder not whitelisted for that folder combo is covered below;
		// this case uses an entirely unknown subfolder on a known folder
		final var request = new PresignUploadRequestModel("post", "logo", "x.png");

		final var exception = assertThrows(BusinessException.class, () -> useCase.execute(request));

		assertEquals(ExceptionCode.UPLOAD_FAILED, exception.getCode());
	}

	@Test
	void unknownFolder_throwsBadRequest() {
		final var request = new PresignUploadRequestModel("author", "avatar", "x.png");

		final var exception = assertThrows(BusinessException.class, () -> useCase.execute(request));

		assertEquals(ExceptionCode.UPLOAD_FAILED, exception.getCode());
	}
}
