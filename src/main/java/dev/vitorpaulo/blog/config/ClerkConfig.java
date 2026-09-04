package dev.vitorpaulo.blog.config;

import com.clerk.backend_api.Clerk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClerkConfig  {

	private Clerk clerk;

	@Value("${clerk.api-token}")
	private String clerkToken;

	@Bean
    public Clerk clerk() {
		if (clerk != null) return clerk;

		clerk = Clerk.builder()
			.bearerAuth(clerkToken)
			.build();
		return clerk;
	}
}
