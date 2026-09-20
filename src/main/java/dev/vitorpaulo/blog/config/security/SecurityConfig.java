package dev.vitorpaulo.blog.config.security;

import dev.vitorpaulo.blog.usecase.author.FindOrCreateAuthorUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
	private String issuerUri;

	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		JwtAuthenticationConverter jwtAuthenticationConverter,
		FindOrCreateAuthorUseCase findOrCreateAuthorUseCase
	) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
				.requestMatchers("/actuator/**").hasRole("ADMIN")
				.requestMatchers("/v1/webhook/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/v1/post/*/react").permitAll()
				.requestMatchers(HttpMethod.POST, "/v1/project/*/react").permitAll()
				.requestMatchers(HttpMethod.POST, "/v1/newsletter/subscribe").permitAll()
				.requestMatchers(HttpMethod.POST, "/v1/post/featured").hasRole("ADMIN")
				.requestMatchers(HttpMethod.GET, "/v1/post/featured/**").permitAll()
				.requestMatchers(HttpMethod.GET, "/v1/post/slug/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/v1/post/search").permitAll()
				.requestMatchers(HttpMethod.POST, "/v1/post/batch").permitAll()
				.requestMatchers(HttpMethod.POST, "/v1/post/*/audio/**").authenticated()
				.requestMatchers(HttpMethod.GET, "/v1/post/*").authenticated()
				.requestMatchers(HttpMethod.GET, "/v1/project/slug/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/v1/project/search").permitAll()
				.requestMatchers(HttpMethod.POST, "/v1/project/batch").permitAll()
				.requestMatchers(HttpMethod.GET, "/v1/project/*").authenticated()
				.requestMatchers(HttpMethod.POST, "/v1/tag/search").permitAll()
				.requestMatchers(HttpMethod.POST, "/v1/tag/batch").permitAll()
				.requestMatchers("/v1/tag/**").authenticated()
				.requestMatchers("/v1/newsletter/subscriber/**").authenticated()
				.requestMatchers("/v1/dashboard/**").authenticated()
				.requestMatchers("/v1/upload/**").authenticated()
				.anyRequest().authenticated()
			)
			.addFilterAfter(new AuthorRoleFilter(findOrCreateAuthorUseCase), BearerTokenAuthenticationFilter.class)
			.oauth2ResourceServer(oauth2 -> oauth2
				.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
			);

		return http.build();
	}

	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();

		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			Collection<GrantedAuthority> authorities = new ArrayList<>();

			collectClaimedAuthorities(jwt.getClaim("org_role"), authorities);

			Collection<GrantedAuthority> scopeAuthorities = scopeConverter.convert(jwt);
			if (scopeAuthorities != null) {
				for (GrantedAuthority ga : scopeAuthorities) {
					if (authorities.stream().noneMatch(a -> a.getAuthority().equals(ga.getAuthority()))) {
						authorities.add(ga);
					}
				}
			}

			return authorities;
		});
		return converter;
	}

	private void collectClaimedAuthorities(Object claim, Collection<GrantedAuthority> authorities) {
		if (claim instanceof String role) {
			authorities.add(AuthorRoleMapper.toAuthority(role));
		} else if (claim instanceof Collection<?> collection) {
			for (Object item : collection) {
				if (item instanceof String role) {
					authorities.add(AuthorRoleMapper.toAuthority(role));
				}
			}
		}
	}

	@Bean
	public JwtDecoder jwtDecoder() {
		return JwtDecoders.fromIssuerLocation(issuerUri);
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		var config = new CorsConfiguration();
		config.addAllowedOriginPattern("http://localhost:*");
		config.addAllowedOriginPattern("http://127.0.0.1:*");
		config.addAllowedOriginPattern("http://192.168.*.*:*");
		config.addAllowedOriginPattern("http://100.72.94.66:4200");
		config.addAllowedOriginPattern("https://vitorpaulo.dev");
		config.addAllowedMethod("*");
		config.addAllowedHeader("*");
		config.setAllowCredentials(true);
		var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
