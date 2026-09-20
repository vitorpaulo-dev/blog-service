package dev.vitorpaulo.blog.config.security;

import dev.vitorpaulo.blog.model.AuthorModel;
import dev.vitorpaulo.blog.usecase.author.FindOrCreateAuthorUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityFilterChainTest.ProbeController.class)
@Import({SecurityConfig.class, SecurityFilterChainTest.ProbeController.class})
class SecurityFilterChainTest {

	private static final String FEATURED_URI = "/v1/post/featured";

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@MockitoBean
	private FindOrCreateAuthorUseCase findOrCreateAuthorUseCase;

	@MockitoBean
	private dev.vitorpaulo.blog.config.captcha.TurnstileClient turnstileClient;

	@Autowired
	private MockMvc mockMvc;

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void featuredAnonymous_returnsUnauthorized() throws Exception {
		mockMvc.perform(post(FEATURED_URI))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void featuredMember_returnsForbidden() throws Exception {
		stubToken("member-token", jwtWithClaim("org:member"));
		mockMvc.perform(post(FEATURED_URI).header("Authorization", "Bearer member-token"))
			.andExpect(status().isForbidden());
	}

	@Test
	void featuredAdmin_returnsOk() throws Exception {
		stubToken("admin-token", jwtWithClaim("org:admin"));
		mockMvc.perform(post(FEATURED_URI).header("Authorization", "Bearer admin-token"))
			.andExpect(status().isOk());
	}

	@Test
	void actuatorHealthAnonymous_passesAuthorizationChain() throws Exception {
		mockMvc.perform(get("/actuator/health"))
			.andExpect(result -> {
				int status = result.getResponse().getStatus();
				if (status == 401 || status == 403) {
					throw new AssertionError("Actuator health should stay public but status was " + status);
				}
			});
	}

	@Test
	void actuatorFlywayAnonymous_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/actuator/flyway"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void actuatorFlywayAdmin_returnsOk() throws Exception {
		stubToken("admin-token", jwtWithClaim("org:admin"));
		mockMvc.perform(get("/actuator/flyway").header("Authorization", "Bearer admin-token"))
			.andExpect(status().isOk());
	}

	@Test
	void unknownProtectedPathAnonymous_returnsUnauthorized() throws Exception {
		mockMvc.perform(post("/v1/tag/" + java.util.UUID.randomUUID() + "/scope"))
			.andExpect(status().isUnauthorized());
	}

	private Jwt jwtWithClaim(String role) {
		return Jwt.withTokenValue("token")
			.header("alg", "none")
			.subject("user_sub")
			.claim("org_role", role)
			.build();
	}

	private void stubToken(String rawToken, Jwt decoded) {
		when(jwtDecoder.decode(eq(rawToken))).thenReturn(decoded);
		when(findOrCreateAuthorUseCase.execute(any(Jwt.class)))
			.thenReturn(new AuthorModel(null, "Author " + rawToken, null, null, null, "org:member"));
	}

	@RestController
	static class ProbeController {

		@PostMapping("/v1/post/featured")
		ResponseEntity<Void> featured() {
			return ResponseEntity.ok().build();
		}

		@GetMapping("/actuator/health")
		ResponseEntity<Void> health() {
			return ResponseEntity.ok().build();
		}

		@GetMapping("/actuator/flyway")
		ResponseEntity<Void> flyway() {
			return ResponseEntity.ok().build();
		}
	}
}
