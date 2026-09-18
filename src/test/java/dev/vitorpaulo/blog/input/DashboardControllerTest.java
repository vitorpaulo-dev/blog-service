package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.input.mapper.DashboardInputMapper;
import dev.vitorpaulo.blog.input.response.DashboardStatsResponse;
import dev.vitorpaulo.blog.input.response.TopPostsResponse;
import dev.vitorpaulo.blog.input.response.TopProjectsResponse;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.DashboardStatsModel;
import dev.vitorpaulo.blog.model.TopPostsModel;
import dev.vitorpaulo.blog.model.TopProjectsModel;
import dev.vitorpaulo.blog.usecase.dashboard.GetDashboardStatsUseCase;
import dev.vitorpaulo.blog.usecase.dashboard.GetTopPostsUseCase;
import dev.vitorpaulo.blog.usecase.dashboard.GetTopProjectsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

	@Mock private GetDashboardStatsUseCase getDashboardStatsUseCase;
	@Mock private GetTopPostsUseCase getTopPostsUseCase;
	@Mock private GetTopProjectsUseCase getTopProjectsUseCase;
	@Mock private DashboardInputMapper dashboardInputMapper;
	@Mock private AuthorModel author;
	@Mock private DashboardStatsModel statsModel;
	@Mock private TopPostsModel topPostsModel;
	@Mock private TopProjectsModel topProjectsModel;
	@Mock private DashboardStatsResponse statsResponse;
	@Mock private TopPostsResponse topPostsResponse;
	@Mock private TopProjectsResponse topProjectsResponse;

	@InjectMocks
	private DashboardController dashboardController;

	@BeforeEach
	void setUp() {
		lenient().when(author.role()).thenReturn("org:admin");
	}

	@Test
	void stats_admin_returnsMappedResponse() {
		when(getDashboardStatsUseCase.execute()).thenReturn(statsModel);
		when(dashboardInputMapper.toResponse(statsModel)).thenReturn(statsResponse);

		var result = dashboardController.stats(author);

		assertEquals(statsResponse, result);
	}

	@Test
	void stats_nonAdmin_throwsForbidden() {
		when(author.role()).thenReturn("org:member");

		var ex = assertThrows(BusinessException.class, () -> dashboardController.stats(author));

		assertEquals(ExceptionCode.FORBIDDEN, ex.getCode());
		verifyNoInteractions(getDashboardStatsUseCase, dashboardInputMapper);
	}

	@Test
	void topPosts_admin_passesLimitAndReturnsMappedResponse() {
		when(getTopPostsUseCase.execute(5)).thenReturn(topPostsModel);
		when(dashboardInputMapper.toResponse(topPostsModel)).thenReturn(topPostsResponse);

		var result = dashboardController.topPosts(author, 5);

		assertEquals(topPostsResponse, result);
	}

	@Test
	void topPosts_nonAdmin_throwsForbidden() {
		when(author.role()).thenReturn("org:member");

		var ex = assertThrows(BusinessException.class, () -> dashboardController.topPosts(author, 5));

		assertEquals(ExceptionCode.FORBIDDEN, ex.getCode());
		verifyNoInteractions(getTopPostsUseCase, dashboardInputMapper);
	}

	@Test
	void topProjects_admin_passesLimitAndReturnsMappedResponse() {
		when(getTopProjectsUseCase.execute(10)).thenReturn(topProjectsModel);
		when(dashboardInputMapper.toResponse(topProjectsModel)).thenReturn(topProjectsResponse);

		var result = dashboardController.topProjects(author, 10);

		assertEquals(topProjectsResponse, result);
	}

	@Test
	void topProjects_nonAdmin_throwsForbidden() {
		when(author.role()).thenReturn("org:member");

		var ex = assertThrows(BusinessException.class, () -> dashboardController.topProjects(author, 5));

		assertEquals(ExceptionCode.FORBIDDEN, ex.getCode());
		verifyNoInteractions(getTopProjectsUseCase, dashboardInputMapper);
	}
}
