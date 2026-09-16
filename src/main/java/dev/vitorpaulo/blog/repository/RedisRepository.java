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
}
