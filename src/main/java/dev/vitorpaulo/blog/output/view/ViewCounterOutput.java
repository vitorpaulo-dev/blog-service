package dev.vitorpaulo.blog.output.view;

import dev.vitorpaulo.blog.repository.PostRepository;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import dev.vitorpaulo.blog.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCounterOutput {

	private final PostRepository postRepository;
	private final ProjectRepository projectRepository;
	private final RedisRepository redisRepository;

	private static final String POST_KEY_PREFIX = "post:";
	private static final String PROJECT_KEY_PREFIX = "project:";
	private static final String VIEWS_KEY_SUFFIX = ":views";
	private static final Duration VIEW_TTL = Duration.ofHours(48);

	@Async("viewCountTaskExecutor")
	@Transactional
	public void countPostView(UUID postId, String ip) {
		try {
			final var viewKey = POST_KEY_PREFIX + postId + ":view:" + ip;
			if (redisRepository.keyExists(viewKey)) {
				return;
			}

			final var now = System.currentTimeMillis();
			postRepository.incrementViewCount(postId);
			redisRepository.set(viewKey, VIEW_TTL);
			redisRepository.addToSortedSet(
				POST_KEY_PREFIX + postId + VIEWS_KEY_SUFFIX,
				ip + ":" + now,
				now,
				VIEW_TTL
			);
		} catch (Exception e) {
			log.error("Failed to count view for post {}", postId, e);
		}
	}

	@Async("viewCountTaskExecutor")
	@Transactional
	public void countProjectView(UUID projectId, String ip) {
		try {
			final var viewKey = PROJECT_KEY_PREFIX + projectId + ":view:" + ip;
			if (redisRepository.keyExists(viewKey)) {
				return;
			}

			final var now = System.currentTimeMillis();
			projectRepository.incrementViewCount(projectId);
			redisRepository.set(viewKey, VIEW_TTL);
			redisRepository.addToSortedSet(
				PROJECT_KEY_PREFIX + projectId + VIEWS_KEY_SUFFIX,
				ip + ":" + now,
				now,
				VIEW_TTL
			);
		} catch (Exception e) {
			log.error("Failed to count view for project {}", projectId, e);
		}
	}
}
