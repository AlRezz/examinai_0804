package com.examinai.app.web;

/**
 * Session flag set when assistive AI draft generation fails (timeouts / provider errors). Cleared after a successful
 * draft. Used with mentor review UI (FR29 / UX-DR7).
 */
public final class DegradedInferenceAttributes {

	public static final String SESSION_KEY = "degradedInference";

	private DegradedInferenceAttributes() {
	}
}
