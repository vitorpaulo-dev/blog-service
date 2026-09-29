package dev.vitorpaulo.blog.output.upload;

import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.model.upload.SignedUrlModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageOutputTest {

	@Mock private S3Presigner presigner;
	@Mock private S3Presigner uploader;
	@Mock private S3Client s3Deleter;
	@Mock private PresignedPutObjectRequest presignedPut;
	@Mock private PresignedGetObjectRequest presignedGet;

	private static final Duration GET_EXPIRY = Duration.ofHours(1);
	private static final Duration PUT_EXPIRY = Duration.ofMinutes(15);

	@Test
	void presignUpload_buildsUuidKeyWithExtension() throws Exception {
		when(presignedPut.url()).thenReturn(URI.create("https://r2.example/signed-put").toURL());
		when(uploader.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPut);

		final var output = output(Optional.of(presigner), Optional.of(uploader), Optional.of(s3Deleter));
		final var result = output.presignUpload("post", "banner", "my Picture.JPEG");

		assertTrue(result.key().matches("^post/banner/[0-9a-f-]{36}\\.jpeg$"));
		assertEquals("https://r2.example/signed-put", result.url());
		verify(uploader).presignPutObject(argThat((PutObjectPresignRequest request) ->
			"test-bucket".equals(request.putObjectRequest().bucket())
				&& PUT_EXPIRY.equals(request.signatureDuration())));
	}

	@Test
	void presignUpload_fileNameWithoutExtension_keepsKeyExtensionless() throws Exception {
		when(presignedPut.url()).thenReturn(URI.create("https://r2.example/signed-put").toURL());
		when(uploader.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(presignedPut);

		final var output = output(Optional.of(presigner), Optional.of(uploader), Optional.of(s3Deleter));
		final var result = output.presignUpload("project", "logo", "noext");

		assertTrue(result.key().matches("^project/logo/[0-9a-f-]{36}$"));
	}

	@Test
	void signKeys_presignsGetWithHourExpiry() throws Exception {
		when(presignedGet.url()).thenReturn(URI.create("https://r2.example/signed-get").toURL());
		when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedGet);

		final var output = output(Optional.of(presigner), Optional.of(uploader), Optional.of(s3Deleter));
		final List<SignedUrlModel> result = output.signKeys(List.of("post/content/a.png"));

		assertEquals(1, result.size());
		assertEquals("post/content/a.png", result.get(0).key());
		assertEquals("https://r2.example/signed-get", result.get(0).url());
		verify(presigner).presignGetObject(argThat((GetObjectPresignRequest request) ->
			"test-bucket".equals(request.getObjectRequest().bucket())
				&& "post/content/a.png".equals(request.getObjectRequest().key())
				&& GET_EXPIRY.equals(request.signatureDuration())));
	}

	@Test
	void missingPresigner_throwsUploadFailed() throws Exception {
		final var output = output(Optional.empty(), Optional.empty(), Optional.empty());

		final var uploadException = assertThrows(BusinessException.class, () -> output.presignUpload("post", "banner", "a.png"));
		final var signException = assertThrows(BusinessException.class, () -> output.signKeys(List.of("post/banner/a.png")));

		assertEquals(ExceptionCode.UPLOAD_FAILED, uploadException.getCode());
		assertEquals(ExceptionCode.UPLOAD_FAILED, signException.getCode());
	}

	@Test
	void missingUploader_throwsUploadFailedOnPresign() throws Exception {
		when(presignedGet.url()).thenReturn(URI.create("https://r2.example/signed-get").toURL());
		when(presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedGet);

		final var output = output(Optional.of(presigner), Optional.empty(), Optional.of(s3Deleter));

		final var uploadException = assertThrows(BusinessException.class, () -> output.presignUpload("post", "banner", "a.png"));

		assertEquals(ExceptionCode.UPLOAD_FAILED, uploadException.getCode());
		assertEquals(1, output.signKeys(List.of("post/banner/a.png")).size());
	}

	@Test
	void delete_removesObjectFromBucket() throws Exception {
		final var output = output(Optional.of(presigner), Optional.of(uploader), Optional.of(s3Deleter));
		output.delete("post/audio/abc/podcast.wav");

		verify(s3Deleter).deleteObject(argThat((DeleteObjectRequest request) ->
			"test-bucket".equals(request.bucket()) && "post/audio/abc/podcast.wav".equals(request.key())));
	}

	@Test
	void missingDeleter_throwsUploadFailed() throws Exception {
		final var output = output(Optional.of(presigner), Optional.of(uploader), Optional.empty());

		final var exception = assertThrows(BusinessException.class, () -> output.delete("post/audio/abc/podcast.wav"));

		assertEquals(ExceptionCode.UPLOAD_FAILED, exception.getCode());
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
