package dev.vitorpaulo.blog.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Slf4j
@Configuration
public class R2Config {

	@Value("${r2.endpoint:}")
	private String endpoint;

	@Value("${r2.public-endpoint:}")
	private String publicEndpoint;

	@Value("${r2.access-key-id:}")
	private String accessKeyId;

	@Value("${r2.secret-access-key:}")
	private String secretAccessKey;

	@Bean
	public S3Presigner s3Presigner() {
		if (StringUtils.isAnyBlank(publicEndpoint, accessKeyId, secretAccessKey)) {
			return null;
		}

		return S3Presigner.builder()
			.region(Region.of("auto"))
			.endpointOverride(URI.create(publicEndpoint))
			.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
			.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).checksumValidationEnabled(false).build())
			.build();
	}

    @Bean
    public S3Client s3Deleter() {
        if (StringUtils.isAnyBlank(endpoint, accessKeyId, secretAccessKey)) {
            return null;
        }

        return S3Client.builder()
            .region(Region.of("auto"))
            .endpointOverride(URI.create(endpoint))
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
            .build();
    }

    @Bean
    public S3Presigner s3Uploader() {
		if (StringUtils.isAnyBlank(endpoint, accessKeyId, secretAccessKey)) {
			return null;
		}

		return S3Presigner.builder()
			.region(Region.of("auto"))
			.endpointOverride(URI.create(endpoint))
			.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
			.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).checksumValidationEnabled(false).build())
			.build();
	}
}