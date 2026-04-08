package com.examinai.app.integration.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@ConfigurationProperties(prefix = "examinai.ai.llm-health-probe")
public class LlmHealthProbeProperties {

	/**
	 * When true, register an Actuator health contributor that probes the configured Ollama base URL (NFR8).
	 */
	private boolean enabled = false;

	@Min(100)
	@Max(60_000)
	private int connectTimeoutMs = 2_000;

	@Min(100)
	@Max(60_000)
	private int readTimeoutMs = 3_000;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getConnectTimeoutMs() {
		return connectTimeoutMs;
	}

	public void setConnectTimeoutMs(int connectTimeoutMs) {
		this.connectTimeoutMs = connectTimeoutMs;
	}

	public int getReadTimeoutMs() {
		return readTimeoutMs;
	}

	public void setReadTimeoutMs(int readTimeoutMs) {
		this.readTimeoutMs = readTimeoutMs;
	}
}
