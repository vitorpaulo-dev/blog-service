package dev.vitorpaulo.blog.output.upload;

import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.model.upload.SignedUrlModel;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StorageOutputTest {

	private final S3Presigner presigner = mock(S3Presigner.class);
	private final S3Presigner uploader = mock(S3Presigner.class);

	@Test
	void presignUpload_buildsUuidKeyWithExtension() throws Exception {
		final var presigned = mock(PresignedPutObjectRequest.class);
		when(presigned.url()).thenReturn(URI.create("https://r2.example/signed-put").toURL());
		when(uploader.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presigned);

		final var output = output(Optional.of(presigner), Optional.of(uploader));
		final var result = output.presignUpload("post", "banner", "my Picture.JPEG");

		assertTrue(result.key().matches("^post/banner/[0-9a-f-]{36}\\.jpeg$"));
		assertEquals("https://r2.example/signed-put", result.url());
		verify(uploader).presignPutObject(argThat((PutObjectPresignRequest request) -> {
			assertEquals("test-bucket", request.putObjectRequest().bucket());
			assertEquals(Duration.ofMinutes(5), request.signatureDuration());
			return true;
		}));
	}

	@Test
	void presignUpload_fileNameWithoutExtension_keepsKeyExtensionless() throws Exception {
		final var presigned = mock(PresignedPutObjectRequest.class);
		when(presigned.url()).thenReturn(URI.create("https://r2.example/signed-put").toURL());
		when(uploader.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presigned);

		final var output = output(Optional.of(presigner), Optional.of(uploader));
		final var result = output.presignUpload("project", "logo", "noext");

		assertTrue(result.key().matches("^project/logo/[0-9a-f-]{36}$"));
	}

	@Test
	void signKeys_presignsGetWithHourExpiry() throws Exception {
		final var presigned = mock(PresignedGetObjectRequest.class);
		when(presigned.url()).thenReturn(URI.create("https://r2.example/signed-get").toURL());
		when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

		final var output = output(Optional.of(presigner), Optional.of(uploader));
		final List<SignedUrlModel> result = output.signKeys(List.of("post/content/a.png"));

		assertEquals(1, result.size());
		assertEquals("post/content/a.png", result.get(0).key());
		assertEquals("https://r2.example/signed-get", result.get(0).url());
		verify(presigner).presignGetObject(argThat((GetObjectPresignRequest request) ->
			"test-bucket".equals(request.getObjectRequest().bucket())
				&& "post/content/a.png".equals(request.getObjectRequest().key())
				&& Duration.ofHours(1).equals(request.signatureDuration())));
	}

	@Test
	void missingPresigner_throwsUploadFailed() throws Exception {
		final var output = output(Optional.empty(), Optional.empty());

		final var uploadException = assertThrows(BusinessException.class, () -> output.presignUpload("post", "banner", "a.png"));
		final var signException = assertThrows(BusinessException.class, () -> output.signKeys(List.of("post/banner/a.png")));

		assertEquals(ExceptionCode.UPLOAD_FAILED, uploadException.getCode());
		assertEquals(ExceptionCode.UPLOAD_FAILED, signException.getCode());
	}

	@Test
	void missingUploader_throwsUploadFailedOnPresign() throws Exception {
		final var presigned = mock(PresignedGetObjectRequest.class);
		when(presigned.url()).thenReturn(URI.create("https://r2.example/signed-get").toURL());
		when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presigned);

		final var output = output(Optional.of(presigner), Optional.empty());

		final var uploadException = assertThrows(BusinessException.class, () -> output.presignUpload("post", "banner", "a.png"));

		assertEquals(ExceptionCode.UPLOAD_FAILED, uploadException.getCode());
		assertEquals(1, output.signKeys(List.of("post/banner/a.png")).size());
	}

	@Test
	void delete_removesObjectFromBucket() throws Exception {
		final var deleter = mock(S3Client.class);

		final var output = output(Optional.of(presigner), Optional.of(uploader), Optional.of(deleter));
		output.delete("post/audio/abc/podcast.wav");

		verify(deleter).deleteObject(argThat((DeleteObjectRequest request) ->
			"test-bucket".equals(request.bucket()) && "post/audio/abc/podcast.wav".equals(request.key())));
	}

	@Test
	void missingDeleter_throwsUploadFailed() throws Exception {
		final var output = output(Optional.of(presigner), Optional.of(uploader), Optional.empty());

		final var exception = assertThrows(BusinessException.class, () -> output.delete("post/audio/abc/podcast.wav"));

		assertEquals(ExceptionCode.UPLOAD_FAILED, exception.getCode());
	}

	private StorageOutput output(Optional<S3Presigner> readPresigner, Optional<S3Presigner> uploadPresigner)
		throws NoSuchFieldException, IllegalAccessException {
		return output(readPresigner, uploadPresigner, Optional.empty());
	}

	private StorageOutput output(Optional<S3Presigner> readPresigner, Optional<S3Presigner> uploadPresigner,
		Optional<S3Client> deleter)
		throws NoSuchFieldException, IllegalAccessException {
		final var output = new StorageOutput(readPresigner, uploadPresigner, deleter);
		final var bucketField = StorageOutput.class.getDeclaredField("bucket");
		bucketField.setAccessible(true);
		bucketField.set(output, "test-bucket");
		return output;
	}
}
