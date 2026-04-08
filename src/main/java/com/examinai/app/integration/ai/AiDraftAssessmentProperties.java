package com.examinai.app.integration.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@ConfigurationProperties(prefix = "examinai.ai.draft-assessment")
public class AiDraftAssessmentProperties {

	/**
	 * Maximum characters of stored normalized source sent to the model (truncation tail is appended).
	 */
	@Min(1)
	@Max(50_000_000)
	private int maxSourceChars = 100_000;

	/**
	 * Per-attempt wall-clock timeout for the chat call (NFR4).
	 */
	@Min(1)
	@Max(86400)
	private int requestTimeoutSeconds = 90;

	/**
	 * Retries after a failed or timed-out attempt (NFR4).
	 */
	@Min(0)
	@Max(50)
	private int maxRetries = 2;

	@Min(0)
	@Max(600_000)
	private long retryBackoffMs = 400L;

	/**
	 * Hard cap on wall-clock time for all inference attempts (including retries), after the DB read (NFR4 / proxy safety).
	 */
	@Min(1)
	@Max(86400)
	private int maxInferenceWallSeconds = 300;

	/**
	 * Max characters placed in flash/session for one AI draft until persistence exists (story 5.2).
	 */
	@Min(256)
	@Max(2_000_000)
	private int maxFlashChars = 32_768;

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

	public int getMaxFlashChars() {
		return maxFlashChars;
	}

	public void setMaxFlashChars(int maxFlashChars) {
		this.maxFlashChars = maxFlashChars;
	}

	public int getMaxInferenceWallSeconds() {
		return maxInferenceWallSeconds;
	}

	public void setMaxInferenceWallSeconds(int maxInferenceWallSeconds) {
		this.maxInferenceWallSeconds = maxInferenceWallSeconds;
	}
}
