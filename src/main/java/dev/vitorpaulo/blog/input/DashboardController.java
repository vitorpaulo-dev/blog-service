package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.common.exception.infrastructure.BusinessException;
import dev.vitorpaulo.blog.common.exception.infrastructure.ExceptionCode;
import dev.vitorpaulo.blog.config.security.CurrentAuthor;
import dev.vitorpaulo.blog.input.mapper.DashboardInputMapper;
import dev.vitorpaulo.blog.input.response.DashboardStatsResponse;
import dev.vitorpaulo.blog.input.response.TopPostsResponse;
import dev.vitorpaulo.blog.input.response.TopProjectsResponse;
import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.usecase.dashboard.GetDashboardStatsUseCase;
import dev.vitorpaulo.blog.usecase.dashboard.GetTopPostsUseCase;
import dev.vitorpaulo.blog.usecase.dashboard.GetTopProjectsUseCase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static dev.vitorpaulo.blog.common.util.RoleUtils.isAdmin;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@Validated
public class DashboardController {

	private final GetDashboardStatsUseCase getDashboardStatsUseCase;
	private final GetTopPostsUseCase getTopPostsUseCase;
	private final GetTopProjectsUseCase getTopProjectsUseCase;
	private final DashboardInputMapper dashboardInputMapper;

	@GetMapping("/stats")
	public DashboardStatsResponse stats(@CurrentAuthor AuthorModel author) {
		authorize(author);
		return dashboardInputMapper.toResponse(getDashboardStatsUseCase.execute());
	}

	@GetMapping("/posts/top")
	public TopPostsResponse topPosts(
			@CurrentAuthor AuthorModel author,
			@RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit
	) {
		authorize(author);
		return dashboardInputMapper.toResponse(getTopPostsUseCase.execute(limit));
	}

	@GetMapping("/projects/top")
	public TopProjectsResponse topProjects(
			@CurrentAuthor AuthorModel author,
			@RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit
	) {
		authorize(author);
		return dashboardInputMapper.toResponse(getTopProjectsUseCase.execute(limit));
	}

	private void authorize(AuthorModel author) {
		if (!isAdmin(author.role())) {
			throw new BusinessException(HttpStatus.FORBIDDEN, ExceptionCode.FORBIDDEN, null);
		}
	}
}
