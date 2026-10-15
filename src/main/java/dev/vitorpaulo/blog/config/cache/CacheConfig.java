package dev.vitorpaulo.blog.config.cache;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration(proxyBeanMethods = false)
@EnableCaching
public class CacheConfig {

	public static final String FEATURED_POSTS = "featured-posts";
	public static final String POST_SEARCH = "post-search";
	public static final String POST_SLUG = "post-slug";
	public static final String PROJECT_SEARCH = "project-search";
	public static final String PROJECT_SLUG = "project-slug";
	public static final String PROJECT_BATCH = "project-batch";
	public static final String TAG_SEARCH = "tag-search";
	public static final String TAG_BATCH = "tag-batch";

	private static final Duration SHORT_TTL = Duration.ofHours(1);
	private static final Duration LONG_TTL = Duration.ofHours(6);

	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		final var configurations = new LinkedHashMap<String, RedisCacheConfiguration>();
		configurations.put(FEATURED_POSTS, cacheConfiguration(LONG_TTL));
		configurations.put(POST_SEARCH, cacheConfiguration(LONG_TTL));
		configurations.put(POST_SLUG, cacheConfiguration(SHORT_TTL));
		configurations.put(PROJECT_SEARCH, cacheConfiguration(LONG_TTL));
		configurations.put(PROJECT_SLUG, cacheConfiguration(SHORT_TTL));
		configurations.put(PROJECT_BATCH, cacheConfiguration(LONG_TTL));
		configurations.put(TAG_SEARCH, cacheConfiguration(LONG_TTL));
		configurations.put(TAG_BATCH, cacheConfiguration(LONG_TTL));

		return RedisCacheManager.builder(connectionFactory)
			.cacheDefaults(cacheConfiguration(SHORT_TTL))
			.withInitialCacheConfigurations(configurations)
			.transactionAware()
			.build();
	}

	static RedisCacheConfiguration cacheConfiguration(Duration ttl) {
		return RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(ttl)
			.disableCachingNullValues()
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer()));
	}

	static RedisSerializer<Object> valueSerializer() {
		return GenericJacksonJsonRedisSerializer.builder()
			.enableDefaultTyping(typeValidator())
			.build();
	}

	private static PolymorphicTypeValidator typeValidator() {
		return BasicPolymorphicTypeValidator.builder()
			.allowIfSubType("dev.vitorpaulo.blog.")
			.allowIfSubType("java.util.")
			.build();
	}
}
