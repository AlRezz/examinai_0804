package com.examinai.app.integration.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Optional connectivity probe to the Ollama HTTP API (same base URL as Spring AI). Enable with
 * {@code examinai.ai.llm-health-probe.enabled=true}.
 */
@Component("llmInference")
@ConditionalOnProperty(prefix = "examinai.ai.llm-health-probe", name = "enabled", havingValue = "true")
public class OllamaLlmHealthIndicator implements HealthIndicator {

	private final String ollamaBaseUrl;

	private final LlmHealthProbeProperties probeProperties;

	public OllamaLlmHealthIndicator(@Value("${spring.ai.ollama.base-url}") String ollamaBaseUrl,
			LlmHealthProbeProperties probeProperties) {
		this.ollamaBaseUrl = ollamaBaseUrl;
		this.probeProperties = probeProperties;
	}

	@Override
	public Health health() {
		URI uri = URI.create(trimTrailingSlash(ollamaBaseUrl) + "/api/tags");
		HttpClient client = HttpClient.newBuilder()
			.connectTimeout(Duration.ofMillis(probeProperties.getConnectTimeoutMs()))
			.build();
		HttpRequest request = HttpRequest.newBuilder(uri)
			.timeout(Duration.ofMillis(probeProperties.getReadTimeoutMs()))
			.GET()
			.build();
		try {
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() >= 200 && response.statusCode() < 300) {
				return Health.up()
					.withDetail("ollamaBaseUrl", trimTrailingSlash(ollamaBaseUrl))
					.withDetail("probePath", "/api/tags")
					.build();
			}
			return Health.down()
				.withDetail("ollamaBaseUrl", trimTrailingSlash(ollamaBaseUrl))
				.withDetail("statusCode", response.statusCode())
				.build();
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return Health.down()
				.withException(ex)
				.withDetail("ollamaBaseUrl", trimTrailingSlash(ollamaBaseUrl))
				.build();
		}
		catch (IOException ex) {
			return Health.down()
				.withException(ex)
				.withDetail("ollamaBaseUrl", trimTrailingSlash(ollamaBaseUrl))
				.build();
		}
	}

	private static String trimTrailingSlash(String url) {
		if (url == null || url.isEmpty()) {
			return url;
		}
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}
}
