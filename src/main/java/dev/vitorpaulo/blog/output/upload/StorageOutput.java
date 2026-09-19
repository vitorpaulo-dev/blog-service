package dev.vitorpaulo.blog.output.upload;

import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.model.upload.SignedUrlModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StorageOutput {

	private static final Duration PUT_EXPIRY = Duration.ofMinutes(15);
	private static final Duration GET_EXPIRY = Duration.ofHours(1);

	private final Optional<S3Presigner> s3Presigner;
	private final Optional<S3Presigner> s3Uploader;
	private final Optional<S3Client> s3Deleter;

	@Value("${r2.bucket:}")
	private String bucket;

	public SignedUrlModel presignUpload(String folder, String subfolder, String fileName) {
		final var key = folder + "/" + subfolder + "/" + UUID.randomUUID() + normalizeExtension(fileName);
		final var presignRequest = PutObjectPresignRequest.builder()
			.signatureDuration(PUT_EXPIRY)
			.putObjectRequest(PutObjectRequest.builder().bucket(bucket).key(key).build())
			.build();

		return new SignedUrlModel(key, uploader().presignPutObject(presignRequest).url().toString());
	}

	public List<SignedUrlModel> signKeys(List<String> keys) {
		return keys.stream()
			.map(this::signKey)
			.toList();
	}

	private SignedUrlModel signKey(String key) {
		final var presignRequest = GetObjectPresignRequest.builder()
			.signatureDuration(GET_EXPIRY)
			.getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(key).build())
			.build();

		return new SignedUrlModel(key, presigner().presignGetObject(presignRequest).url().toString());
	}

	public void delete(String key) {
		s3Deleter.orElseThrow(() ->
			new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionCode.UPLOAD_FAILED, null))
			.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
	}

	private S3Presigner presigner() {
		return s3Presigner.orElseThrow(() ->
			new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionCode.UPLOAD_FAILED, null));
	}

	private S3Presigner uploader() {
		return s3Uploader.orElseThrow(() ->
			new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ExceptionCode.UPLOAD_FAILED, null));
	}

	private String normalizeExtension(String fileName) {
		final var dot = fileName.lastIndexOf('.');
		if (dot < 0 || dot == fileName.length() - 1) {
			return "";
		}

		return fileName.substring(dot).toLowerCase();
	}
}