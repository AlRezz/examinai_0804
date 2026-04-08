package com.examinai.app.integration.ai;

/**
 * Raised when Spring AI inference cannot complete in time or fails after retries (NFR4).
 * Reserved for degraded UX handling in later stories (e.g. 5.4).
 */
public class InferenceUnavailableException extends RuntimeException {

	public InferenceUnavailableException(String message) {
		super(message);
	}

	public InferenceUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
