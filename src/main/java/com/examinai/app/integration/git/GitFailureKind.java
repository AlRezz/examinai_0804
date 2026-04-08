package com.examinai.app.integration.git;

public enum GitFailureKind {

	CONFIG_MISSING,
	ACCESS_DENIED,
	NOT_FOUND,
	RATE_LIMIT,
	TIMEOUT,
	UPSTREAM_ERROR,
	INVALID_RESPONSE
}
