package dev.vitorpaulo.blog.output.view;

import dev.vitorpaulo.blog.config.cache.CacheConfig;
import dev.vitorpaulo.blog.repository.PostRepository;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import dev.vitorpaulo.blog.repository.RedisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViewCounterOutputTest {

    @Mock private PostRepository postRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private RedisRepository redisRepository;
    @Mock private CacheManager cacheManager;
    @Mock private Cache postSlugCache;
    @Mock private Cache projectSlugCache;

    @InjectMocks
    private ViewCounterOutput viewCounterOutput;

    @Test
    void countPostView_dedupKeyPresent_skipsDbUpdateAndRedisWrites() {
        var postId = UUID.randomUUID();
        when(redisRepository.keyExists("post:" + postId + ":view:203.0.113.7")).thenReturn(true);

        viewCounterOutput.countPostView(postId, "203.0.113.7");

        verify(postRepository, never()).incrementViewCount(any());
        verify(redisRepository, never()).set(anyString(), any());
        verify(redisRepository, never()).addToSortedSet(anyString(), anyString(), anyDouble(), any());
        verifyNoInteractions(cacheManager);
    }

    @Test
    void countPostView_dedupKeyAbsent_incrementsAtomicallyAndWritesRedis() {
        var postId = UUID.randomUUID();
        when(redisRepository.keyExists("post:" + postId + ":view:203.0.113.7")).thenReturn(false);
        when(cacheManager.getCache(CacheConfig.POST_SLUG)).thenReturn(postSlugCache);

        viewCounterOutput.countPostView(postId, "203.0.113.7");

        verify(postRepository).incrementViewCount(postId);
        verify(redisRepository).set("post:" + postId + ":view:203.0.113.7", Duration.ofHours(48));
        verify(redisRepository).addToSortedSet(
            eq("post:" + postId + ":views"),
            startsWith("203.0.113.7:"),
            anyDouble(),
            eq(Duration.ofHours(48))
        );
        verify(postSlugCache).clear();
    }

    @Test
    void countPostView_activeTransactionSynchronization_defersEvictionUntilCommit() {
        var postId = UUID.randomUUID();
        when(redisRepository.keyExists("post:" + postId + ":view:203.0.113.7")).thenReturn(false);
        when(cacheManager.getCache(CacheConfig.POST_SLUG)).thenReturn(postSlugCache);

        TransactionSynchronizationManager.initSynchronization();
        try {
            viewCounterOutput.countPostView(postId, "203.0.113.7");

            verify(postSlugCache, never()).clear();

            TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

            verify(postSlugCache).clear();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void countProjectView_dedupKeyPresent_skipsDbUpdateAndRedisWrites() {
        var projectId = UUID.randomUUID();
        when(redisRepository.keyExists("project:" + projectId + ":view:203.0.113.7")).thenReturn(true);

        viewCounterOutput.countProjectView(projectId, "203.0.113.7");

        verify(projectRepository, never()).incrementViewCount(any());
        verify(redisRepository, never()).set(anyString(), any());
        verify(redisRepository, never()).addToSortedSet(anyString(), anyString(), anyDouble(), any());
        verifyNoInteractions(cacheManager);
    }

    @Test
    void countProjectView_dedupKeyAbsent_incrementsAtomicallyAndWritesRedis() {
        var projectId = UUID.randomUUID();
        when(redisRepository.keyExists("project:" + projectId + ":view:203.0.113.7")).thenReturn(false);
        when(cacheManager.getCache(CacheConfig.PROJECT_SLUG)).thenReturn(projectSlugCache);

        viewCounterOutput.countProjectView(projectId, "203.0.113.7");

        verify(projectRepository).incrementViewCount(projectId);
        verify(redisRepository).set("project:" + projectId + ":view:203.0.113.7", Duration.ofHours(48));
        verify(redisRepository).addToSortedSet(
            eq("project:" + projectId + ":views"),
            startsWith("203.0.113.7:"),
            anyDouble(),
            eq(Duration.ofHours(48))
        );
        verify(projectSlugCache).clear();
    }

    @Test
    void countPostView_dbFailure_swallowsException() {
        var postId = UUID.randomUUID();
        when(redisRepository.keyExists(anyString())).thenReturn(false);
        doThrow(new RuntimeException("db down")).when(postRepository).incrementViewCount(postId);

        assertDoesNotThrow(() -> viewCounterOutput.countPostView(postId, "203.0.113.7"));

        verify(redisRepository, never()).set(anyString(), any());
        verify(redisRepository, never()).addToSortedSet(anyString(), anyString(), anyDouble(), any());
        verifyNoInteractions(cacheManager);
    }

    @Test
    void countProjectView_redisFailure_swallowsException() {
        var projectId = UUID.randomUUID();
        when(redisRepository.keyExists(anyString())).thenReturn(false);
        doThrow(new RuntimeException("redis down"))
            .when(redisRepository).addToSortedSet(anyString(), anyString(), anyDouble(), any());

        assertDoesNotThrow(() -> viewCounterOutput.countProjectView(projectId, "203.0.113.7"));

        verify(projectRepository).incrementViewCount(projectId);
        verifyNoInteractions(cacheManager);
    }
}
