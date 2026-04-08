package com.examinai.app.integration.git;

/**
 * Typed integration failure; message must never include secrets (safe for UX mapping in Story 3.3).
 */
public class GitProviderException extends RuntimeException {

	private final GitFailureKind kind;

	public GitProviderException(GitFailureKind kind, String safeMessage) {
		super(safeMessage);
		this.kind = kind;
	}

	public GitProviderException(GitFailureKind kind, String safeMessage, Throwable cause) {
		super(safeMessage, cause);
		this.kind = kind;
	}

	public GitFailureKind getKind() {
		return kind;
	}
}
