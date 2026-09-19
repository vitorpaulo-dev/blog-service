package dev.vitorpaulo.blog.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisRepositoryTest {

    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private ZSetOperations<String, String> zSetOperations;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisRepository redisRepository;

    @BeforeEach
    void setUp() {
        lenient().when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    void addToSortedSet_addsMemberWithScoreAndAppliesTtl() {
        redisRepository.addToSortedSet("post:abc:views", "1.2.3.4:1720000000", 1720000000.0, Duration.ofHours(48));

        verify(zSetOperations).add("post:abc:views", "1.2.3.4:1720000000", 1720000000.0);
        verify(stringRedisTemplate).expire("post:abc:views", Duration.ofHours(48));
    }

    @Test
    void countInRange_countsMembersBetweenMinAndMax() {
        when(zSetOperations.count("post:abc:views", 0.0, 86400000.0)).thenReturn(3L);

        var result = redisRepository.countInRange("post:abc:views", 0.0, 86400000.0);

        assertEquals(3L, result);
    }

    @Test
    void getValue_returnsStoredValue() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("post:abc")).thenReturn("payload");

        var result = redisRepository.getValue("post:abc");

        assertTrue(result.isPresent());
        assertEquals("payload", result.get());
    }

    @Test
    void getValue_missingKey_returnsEmpty() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("post:abc")).thenReturn(null);

        var result = redisRepository.getValue("post:abc");

        assertTrue(result.isEmpty());
    }

    @Test
    void countInRange_nullCount_returnsZero() {
        when(zSetOperations.count("missing", 0.0, 1.0)).thenReturn(null);

        var result = redisRepository.countInRange("missing", 0.0, 1.0);

        assertEquals(0L, result);
    }
}
