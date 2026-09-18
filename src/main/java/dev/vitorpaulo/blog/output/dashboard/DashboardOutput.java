package dev.vitorpaulo.blog.output.dashboard;

import dev.vitorpaulo.blog.model.DashboardStatsModel;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostStatus;
import dev.vitorpaulo.blog.model.ProjectStatus;
import dev.vitorpaulo.blog.model.SubscriberStatus;
import dev.vitorpaulo.blog.model.TopItemModel;
import dev.vitorpaulo.blog.model.TopPostsModel;
import dev.vitorpaulo.blog.model.TopProjectsModel;
import dev.vitorpaulo.blog.repository.PostRepository;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import dev.vitorpaulo.blog.repository.RedisRepository;
import dev.vitorpaulo.blog.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DashboardOutput {

	private static final String POST_KEY_PREFIX = "post:";
	private static final String PROJECT_KEY_PREFIX = "project:";
	private static final String VIEWS_KEY_SUFFIX = ":views";
	private static final String REACTIONS_KEY_SUFFIX = ":reactions";
	private static final Duration LAST_24_HOURS = Duration.ofHours(24);

	private final PostRepository postRepository;
	private final ProjectRepository projectRepository;
	private final SubscriberRepository subscriberRepository;
	private final RedisRepository redisRepository;

	@Transactional(readOnly = true)
	public DashboardStatsModel getStats() {
		return new DashboardStatsModel(
				postRepository.count(),
				postRepository.countByStatus(PostStatus.PUBLISHED),
				postRepository.countByStatus(PostStatus.DRAFT),
				projectRepository.count(),
				projectRepository.countByStatus(ProjectStatus.PUBLISHED),
				projectRepository.countByStatus(ProjectStatus.DRAFT),
				sum(postRepository.sumViewCount()) + sum(projectRepository.sumViewCount()),
				sum(postRepository.sumReactionCount()) + sum(projectRepository.sumReactionCount()),
				subscriberRepository.count(),
				subscriberRepository.countByStatus(SubscriberStatus.ACTIVE)
		);
	}

	@Transactional(readOnly = true)
	public TopPostsModel getTopPosts(int limit) {
		final var allTime = postRepository.findTopByViewCount(PageRequest.of(0, limit), Language.ENGLISH);
		return new TopPostsModel(allTime, last24h(allTime, POST_KEY_PREFIX));
	}

	@Transactional(readOnly = true)
	public TopProjectsModel getTopProjects(int limit) {
		final var allTime = projectRepository.findTopByViewCount(PageRequest.of(0, limit), Language.ENGLISH);
		return new TopProjectsModel(allTime, last24h(allTime, PROJECT_KEY_PREFIX));
	}

	private List<TopItemModel> last24h(List<TopItemModel> items, String keyPrefix) {
		final var window = window();

		return items.stream()
				.map(item -> new TopItemModel(
						item.id(),
						item.title(),
						item.slug(),
						redisRepository.countInRange(keyPrefix + item.id() + VIEWS_KEY_SUFFIX, windowStart(window), windowEnd(window)),
						redisRepository.countInRange(keyPrefix + item.id() + REACTIONS_KEY_SUFFIX, windowStart(window), windowEnd(window)),
						item.createdAt()
				))
				.sorted(rank())
				.toList();
	}

	private static long[] window() {
		final var windowEnd = Instant.now().toEpochMilli();
		return new long[]{windowEnd - LAST_24_HOURS.toMillis(), windowEnd};
	}

	private static long windowStart(long[] window) {
		return window[0];
	}

	private static long windowEnd(long[] window) {
		return window[1];
	}

	private static Comparator<TopItemModel> rank() {
		return Comparator.comparingLong(TopItemModel::viewCount)
				.thenComparingLong(TopItemModel::reactionCount)
				.reversed();
	}

	private long sum(Long value) {
		return value == null ? 0L : value;
	}
}
