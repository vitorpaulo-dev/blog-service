package dev.vitorpaulo.blog.output.dashboard;

import dev.vitorpaulo.blog.model.DashboardStatsModel;
import dev.vitorpaulo.blog.model.Language;
import dev.vitorpaulo.blog.model.PostStatus;
import dev.vitorpaulo.blog.model.ProjectStatus;
import dev.vitorpaulo.blog.model.SubscriberStatus;
import dev.vitorpaulo.blog.model.TopItemModel;
import dev.vitorpaulo.blog.repository.PostRepository;
import dev.vitorpaulo.blog.repository.ProjectRepository;
import dev.vitorpaulo.blog.repository.RedisRepository;
import dev.vitorpaulo.blog.repository.SubscriberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardOutputTest {

	@Mock private PostRepository postRepository;
	@Mock private ProjectRepository projectRepository;
	@Mock private SubscriberRepository subscriberRepository;
	@Mock private RedisRepository redisRepository;

	@InjectMocks
	private DashboardOutput dashboardOutput;

	@Test
	void getStats_sumsCountsAcrossRepositories() {
		when(postRepository.count()).thenReturn(10L);
		when(postRepository.countByStatus(PostStatus.PUBLISHED)).thenReturn(7L);
		when(postRepository.countByStatus(PostStatus.DRAFT)).thenReturn(3L);
		when(projectRepository.count()).thenReturn(5L);
		when(projectRepository.countByStatus(ProjectStatus.PUBLISHED)).thenReturn(4L);
		when(projectRepository.countByStatus(ProjectStatus.DRAFT)).thenReturn(1L);
		when(postRepository.sumViewCount()).thenReturn(120L);
		when(projectRepository.sumViewCount()).thenReturn(30L);
		when(postRepository.sumReactionCount()).thenReturn(40L);
		when(projectRepository.sumReactionCount()).thenReturn(10L);
		when(subscriberRepository.count()).thenReturn(50L);
		when(subscriberRepository.countByStatus(SubscriberStatus.ACTIVE)).thenReturn(45L);

		var result = dashboardOutput.getStats();

		assertEquals(new DashboardStatsModel(10, 7, 3, 5, 4, 1, 150, 50, 50, 45), result);
	}

	@Test
	void getStats_nullSums_treatedAsZero() {
		when(postRepository.count()).thenReturn(0L);
		when(postRepository.countByStatus(any())).thenReturn(0L);
		when(projectRepository.count()).thenReturn(0L);
		when(projectRepository.countByStatus(any())).thenReturn(0L);
		when(postRepository.sumViewCount()).thenReturn(null);
		when(projectRepository.sumViewCount()).thenReturn(null);
		when(postRepository.sumReactionCount()).thenReturn(null);
		when(projectRepository.sumReactionCount()).thenReturn(null);
		when(subscriberRepository.count()).thenReturn(0L);
		when(subscriberRepository.countByStatus(any())).thenReturn(0L);

		var result = dashboardOutput.getStats();

		assertEquals(0L, result.totalViews());
		assertEquals(0L, result.totalReactions());
	}

	@Test
	void getStats_activeSubscribers_readsSubscriberStatus() {
		when(postRepository.count()).thenReturn(0L);
		when(postRepository.countByStatus(any())).thenReturn(0L);
		when(projectRepository.count()).thenReturn(0L);
		when(projectRepository.countByStatus(any())).thenReturn(0L);
		when(postRepository.sumViewCount()).thenReturn(0L);
		when(projectRepository.sumViewCount()).thenReturn(0L);
		when(postRepository.sumReactionCount()).thenReturn(0L);
		when(projectRepository.sumReactionCount()).thenReturn(0L);
		when(subscriberRepository.count()).thenReturn(5L);
		when(subscriberRepository.countByStatus(SubscriberStatus.ACTIVE)).thenReturn(2L);

		var result = dashboardOutput.getStats();

		assertEquals(5L, result.totalSubscribers());
		assertEquals(2L, result.activeSubscribers());
	}

	@Test
	void getTopPosts_fetchesTopByViewCountAndCountsWindowPerEntity() {
		var id = UUID.randomUUID();
		var allTime = topItem(id, 120);
		when(postRepository.findTopByViewCount(PageRequest.of(0, 5), Language.ENGLISH)).thenReturn(List.of(allTime));
		when(redisRepository.countInRange(contains(":views"), anyDouble(), anyDouble())).thenReturn(7L);
		when(redisRepository.countInRange(contains(":reactions"), anyDouble(), anyDouble())).thenReturn(3L);

		var result = dashboardOutput.getTopPosts(5);

		assertEquals(List.of(allTime), result.allTime());
		assertEquals(List.of(new TopItemModel(id, allTime.title(), allTime.slug(), 7L, 3L, allTime.createdAt())), result.last24h());
	}

	@Test
	void getTopProjects_fetchesTopByViewCountAndCountsWindowPerEntity() {
		var id = UUID.randomUUID();
		var allTime = topItem(id, 40);
		when(projectRepository.findTopByViewCount(PageRequest.of(0, 5), Language.ENGLISH)).thenReturn(List.of(allTime));
		when(redisRepository.countInRange(contains(":views"), anyDouble(), anyDouble())).thenReturn(2L);
		when(redisRepository.countInRange(contains(":reactions"), anyDouble(), anyDouble())).thenReturn(1L);

		var result = dashboardOutput.getTopProjects(5);

		assertEquals(List.of(allTime), result.allTime());
		assertEquals(List.of(new TopItemModel(id, allTime.title(), allTime.slug(), 2L, 1L, allTime.createdAt())), result.last24h());
	}

	@Test
	void getTopPosts_sortsLast24hDescending() {
		var idA = UUID.randomUUID();
		var idB = UUID.randomUUID();
		var oneView = topItem(idA, 1);
		var fiveViews = topItem(idB, 5);
		when(postRepository.findTopByViewCount(PageRequest.of(0, 5), Language.ENGLISH)).thenReturn(List.of(oneView, fiveViews));
		when(redisRepository.countInRange(anyString(), anyDouble(), anyDouble())).thenAnswer(inv -> {
			final var key = inv.getArgument(0, String.class);
			if (key.equals("post:" + idB + ":views")) return 5L;
			return key.contains(":views") ? 1L : 0L;
		});

		var result = dashboardOutput.getTopPosts(5);

		assertEquals(List.of(fiveViews, oneView), result.last24h());
	}

	private static TopItemModel topItem(UUID id, long viewCount) {
		return new TopItemModel(id, "Title", "title", viewCount, 0, OffsetDateTime.now());
	}
}
