package dev.vitorpaulo.blog.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResendConfig {

    @Value("${resend.api.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor resendAuthInterceptor() {
        return template -> template.header("Authorization", "Bearer " + apiKey);
    }
}
