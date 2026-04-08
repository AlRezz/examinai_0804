package com.examinai.app.web.task;

import com.examinai.app.integration.git.GitFailureKind;

/**
 * Stable, secret-safe copy for mentors (Story 3.3).
 */
public final class GitRetrievalUiMessage {

	private GitRetrievalUiMessage() {
	}

	public static String forErrorCode(String code) {
		if (code == null || code.isBlank()) {
			return "Source could not be retrieved.";
		}
		try {
			return forKind(GitFailureKind.valueOf(code));
		}
		catch (IllegalArgumentException ex) {
			return "Source could not be retrieved.";
		}
	}

	public static String forKind(GitFailureKind kind) {
		return switch (kind) {
			case CONFIG_MISSING -> "Source retrieval is not configured on the server. An operator must set Git provider environment variables.";
			case ACCESS_DENIED -> "The Git host denied access. Ask an administrator to check credentials or repository visibility.";
			case NOT_FOUND -> "Repository, commit, or path was not found. Confirm coordinates with the intern or update them below.";
			case RATE_LIMIT -> "The Git host rate limit was hit. Wait a few minutes, then try again.";
			case TIMEOUT -> "Fetching source timed out. Try again or narrow the file path scope.";
			case UPSTREAM_ERROR -> "The Git host had a temporary error. Try again shortly.";
			case INVALID_RESPONSE -> "The Git response could not be used. Try a different path scope or coordinates.";
		};
	}
}
