package com.examinai.app.integration.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "examinai.ai.draft-assessment")
public class AiDraftAssessmentProperties {

	/**
	 * Maximum characters of stored normalized source sent to the model (truncation tail is appended).
	 */
	private int maxSourceChars = 100_000;

	/**
	 * Per-attempt wall-clock timeout for the chat call (NFR4).
	 */
	private int requestTimeoutSeconds = 120;

	/**
	 * Retries after a failed or timed-out attempt (NFR4).
	 */
	private int maxRetries = 2;

	private long retryBackoffMs = 400L;

	public int getMaxSourceChars() {
		return maxSourceChars;
	}

	public void setMaxSourceChars(int maxSourceChars) {
		this.maxSourceChars = maxSourceChars;
	}

	public int getRequestTimeoutSeconds() {
		return requestTimeoutSeconds;
	}

	public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
		this.requestTimeoutSeconds = requestTimeoutSeconds;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public long getRetryBackoffMs() {
		return retryBackoffMs;
	}

	public void setRetryBackoffMs(long retryBackoffMs) {
		this.retryBackoffMs = retryBackoffMs;
	}
}
