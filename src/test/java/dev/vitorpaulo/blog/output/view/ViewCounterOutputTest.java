package dev.vitorpaulo.blog.output.view;

import dev.vitorpaulo.blog.repository.PostRepository;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import dev.vitorpaulo.blog.repository.RedisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ViewCounterOutputTest {

    @Mock private PostRepository postRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private RedisRepository redisRepository;

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
    }

    @Test
    void countPostView_dedupKeyAbsent_incrementsAtomicallyAndWritesRedis() {
        var postId = UUID.randomUUID();
        when(redisRepository.keyExists("post:" + postId + ":view:203.0.113.7")).thenReturn(false);

        viewCounterOutput.countPostView(postId, "203.0.113.7");

        verify(postRepository).incrementViewCount(postId);
        verify(redisRepository).set("post:" + postId + ":view:203.0.113.7", Duration.ofHours(48));
        verify(redisRepository).addToSortedSet(
            eq("post:" + postId + ":views"),
            startsWith("203.0.113.7:"),
            anyDouble(),
            eq(Duration.ofHours(48))
        );
    }

    @Test
    void countProjectView_dedupKeyPresent_skipsDbUpdateAndRedisWrites() {
        var projectId = UUID.randomUUID();
        when(redisRepository.keyExists("project:" + projectId + ":view:203.0.113.7")).thenReturn(true);

        viewCounterOutput.countProjectView(projectId, "203.0.113.7");

        verify(projectRepository, never()).incrementViewCount(any());
        verify(redisRepository, never()).set(anyString(), any());
        verify(redisRepository, never()).addToSortedSet(anyString(), anyString(), anyDouble(), any());
    }

    @Test
    void countProjectView_dedupKeyAbsent_incrementsAtomicallyAndWritesRedis() {
        var projectId = UUID.randomUUID();
        when(redisRepository.keyExists("project:" + projectId + ":view:203.0.113.7")).thenReturn(false);

        viewCounterOutput.countProjectView(projectId, "203.0.113.7");

        verify(projectRepository).incrementViewCount(projectId);
        verify(redisRepository).set("project:" + projectId + ":view:203.0.113.7", Duration.ofHours(48));
        verify(redisRepository).addToSortedSet(
            eq("project:" + projectId + ":views"),
            startsWith("203.0.113.7:"),
            anyDouble(),
            eq(Duration.ofHours(48))
        );
    }

    @Test
    void countPostView_dbFailure_swallowsException() {
        var postId = UUID.randomUUID();
        when(redisRepository.keyExists(anyString())).thenReturn(false);
        doThrow(new RuntimeException("db down")).when(postRepository).incrementViewCount(postId);

        assertDoesNotThrow(() -> viewCounterOutput.countPostView(postId, "203.0.113.7"));

        verify(redisRepository, never()).set(anyString(), any());
        verify(redisRepository, never()).addToSortedSet(anyString(), anyString(), anyDouble(), any());
    }

    @Test
    void countProjectView_redisFailure_swallowsException() {
        var projectId = UUID.randomUUID();
        when(redisRepository.keyExists(anyString())).thenReturn(false);
        doThrow(new RuntimeException("redis down"))
            .when(redisRepository).addToSortedSet(anyString(), anyString(), anyDouble(), any());

        assertDoesNotThrow(() -> viewCounterOutput.countProjectView(projectId, "203.0.113.7"));

        verify(projectRepository).incrementViewCount(projectId);
    }
}
