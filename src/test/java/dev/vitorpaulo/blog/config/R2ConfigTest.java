package dev.vitorpaulo.blog.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class R2ConfigTest {

	private final R2Config config = new R2Config();

	@Test
	void blankPublicEndpoint_returnsNullReadPresigner() {
		set("endpoint", "https://account.r2.cloudflarestorage.com");
		set("publicEndpoint", "");
		set("accessKeyId", "key");
		set("secretAccessKey", "secret");

		assertNull(config.s3Presigner());
	}

	@Test
	void blankCredentials_returnNullPresigners() {
		set("endpoint", "");
		set("publicEndpoint", "https://cdn.vitorpaulo.dev");
		set("accessKeyId", "");
		set("secretAccessKey", "");

		assertNull(config.s3Presigner());
		assertNull(config.s3Uploader());
	}

	@Test
	void configuredProperties_buildBothPresigners() {
		set("endpoint", "https://account.r2.cloudflarestorage.com");
		set("publicEndpoint", "https://cdn.vitorpaulo.dev");
		set("accessKeyId", "key");
		set("secretAccessKey", "secret");

		assertNotNull(config.s3Presigner());
		assertNotNull(config.s3Uploader());
	}

	private void set(String field, String value) {
		ReflectionTestUtils.setField(config, field, value);
	}
}
