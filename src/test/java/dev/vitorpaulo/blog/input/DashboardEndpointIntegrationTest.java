package dev.vitorpaulo.blog.input;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.model.DashboardStatsModel;
import dev.vitorpaulo.blog.model.TopItemModel;
import dev.vitorpaulo.blog.model.TopPostsModel;
import dev.vitorpaulo.blog.usecase.author.FindOrCreateAuthorUseCase;
import dev.vitorpaulo.blog.usecase.dashboard.GetDashboardStatsUseCase;
import dev.vitorpaulo.blog.usecase.dashboard.GetTopPostsUseCase;
import dev.vitorpaulo.blog.usecase.dashboard.GetTopProjectsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardEndpointIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@MockitoBean
	private FindOrCreateAuthorUseCase findOrCreateAuthorUseCase;

	@MockitoBean
	private GetDashboardStatsUseCase getDashboardStatsUseCase;

	@MockitoBean
	private GetTopPostsUseCase getTopPostsUseCase;

	@MockitoBean
	private GetTopProjectsUseCase getTopProjectsUseCase;

	@Test
	void stats_unauthenticated_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/v1/dashboard/stats"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void topPosts_nonAdmin_returnsForbidden() throws Exception {
		var author = authorWithRole("org:member");
		when(findOrCreateAuthorUseCase.execute(any())).thenReturn(author);

		mockMvc.perform(get("/v1/dashboard/posts/top").header("Authorization", "Bearer member-token"))
				.andExpect(status().isForbidden());
	}

	@Test
	void topPosts_defaultLimit_isFive() throws Exception {
		var author = authorWithRole("org:admin");
		when(findOrCreateAuthorUseCase.execute(any())).thenReturn(author);
		when(getTopPostsUseCase.execute(5)).thenReturn(new TopPostsModel(List.of(), List.of()));

		mockMvc.perform(get("/v1/dashboard/posts/top").header("Authorization", "Bearer admin-token"))
				.andExpect(status().isOk());

		verify(getTopPostsUseCase).execute(5);
	}

	@Test
	void topPosts_admin_returnsPayload() throws Exception {
		var author = authorWithRole("org:admin");
		var item = new TopItemModel(UUID.randomUUID(), "My Post", "my-post", 100, 10, Instant.now().atOffset(java.time.ZoneOffset.UTC));
		when(findOrCreateAuthorUseCase.execute(any())).thenReturn(author);
		when(getTopPostsUseCase.execute(5)).thenReturn(new TopPostsModel(List.of(item), List.of()));

		mockMvc.perform(get("/v1/dashboard/posts/top").header("Authorization", "Bearer admin-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.allTime[0].title").value("My Post"))
				.andExpect(jsonPath("$.allTime[0].viewCount").value(100))
				.andExpect(jsonPath("$.last24h").isArray());
	}

	@Test
	void topPosts_limitAboveMax_returnsBadRequest() throws Exception {
		var author = authorWithRole("org:admin");
		when(findOrCreateAuthorUseCase.execute(any())).thenReturn(author);

		mockMvc.perform(get("/v1/dashboard/posts/top").header("Authorization", "Bearer admin-token")
						.queryParam("limit", "25"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void stats_admin_returnsPayload() throws Exception {
		var author = authorWithRole("org:admin");
		when(findOrCreateAuthorUseCase.execute(any())).thenReturn(author);
		when(getDashboardStatsUseCase.execute()).thenReturn(new DashboardStatsModel(1, 1, 0, 2, 2, 0, 10, 4, 3, 2));

		mockMvc.perform(get("/v1/dashboard/stats").header("Authorization", "Bearer admin-token"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalPosts").value(1))
				.andExpect(jsonPath("$.activeSubscribers").value(2));
	}

	private Jwt jwt() {
		return new Jwt("raw", Instant.now(), Instant.now().plusSeconds(3600),
				Map.of("alg", "RS256"), Map.of("org_role", "org:admin"));
	}

	private AuthorModel authorWithRole(String role) {
		when(jwtDecoder.decode(any())).thenReturn(jwt());
		return new AuthorModel(null, "user_" + role, null, null, null, role);
	}
}
