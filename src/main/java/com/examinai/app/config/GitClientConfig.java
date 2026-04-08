package com.examinai.app.config;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.examinai.app.integration.git.GitSourceClient;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Wires Git integration beans. Uses {@link RestClient} only (no {@code WebClient}) per architecture / Story 3.2.
 */
@Configuration
@EnableConfigurationProperties(GitProviderProperties.class)
public class GitClientConfig {

	@Bean
	GitSourceClient gitSourceClient(GitProviderProperties properties, RestClient.Builder restClientBuilder) {
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(10));
		factory.setReadTimeout(Duration.ofSeconds(Math.max(5, properties.getReadTimeoutSeconds())));
		String base = properties.getBaseUrl();
		if (!StringUtils.hasText(base)) {
			base = "http://localhost";
		}
		else if (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		RestClient client = restClientBuilder.baseUrl(base).requestFactory(factory).build();
		return new GitSourceClient(properties, client, new ObjectMapper());
	}
}
