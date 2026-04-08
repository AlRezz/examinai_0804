package com.examinai.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Git host integration settings — load only from deployment configuration (env / Spring config), never from HTTP
 * requests or the database (Story 3.1).
 * <p>
 * Bind via {@code examinai.git.*}; example env mapping in {@code application.yml} uses {@code GIT_PROVIDER_BASE_URL}
 * and {@code GIT_PROVIDER_TOKEN}.
 */
@ConfigurationProperties(prefix = "examinai.git")
public class GitProviderProperties {

	/**
	 * Provider REST API base URL, e.g. {@code https://api.github.com} for GitHub REST v3.
	 */
	private String baseUrl = "";

	private String token = "";

	/** Per-attempt read timeout; bounded retries keep total wall time near NFR3 (~60s pilot target). */
	private int readTimeoutSeconds = 22;

	private int maxRetries = 2;

	private int retryBackoffMs = 350;

	public String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token == null ? "" : token.trim();
	}

	public int getReadTimeoutSeconds() {
		return readTimeoutSeconds;
	}

	public void setReadTimeoutSeconds(int readTimeoutSeconds) {
		this.readTimeoutSeconds = readTimeoutSeconds;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public int getRetryBackoffMs() {
		return retryBackoffMs;
	}

	public void setRetryBackoffMs(int retryBackoffMs) {
		this.retryBackoffMs = retryBackoffMs;
	}
}
