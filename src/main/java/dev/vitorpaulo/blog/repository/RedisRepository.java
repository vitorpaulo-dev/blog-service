package dev.vitorpaulo.blog.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisRepository {

	private static final String DEFAULT_VALUE = "1";

	private final StringRedisTemplate stringRedisTemplate;

	public boolean keyExists(String key) {
		return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
	}

	public void set(String key, Duration ttl) {
		stringRedisTemplate.opsForValue().set(key, DEFAULT_VALUE, ttl);
	}

	public void addToSortedSet(String key, String member, double score, Duration ttl) {
		stringRedisTemplate.opsForZSet().add(key, member, score);
		stringRedisTemplate.expire(key, ttl);
	}

	public long countInRange(String key, double min, double max) {
		final var count = stringRedisTemplate.opsForZSet().count(key, min, max);
		return count == null ? 0L : count;
	}
}
