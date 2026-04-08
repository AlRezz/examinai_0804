package com.examinai.app.integration.git;

import java.util.regex.Pattern;

/**
 * Makes arbitrary strings safer for logs (Story 3.1 — no tokens or secret-bearing URLs).
 */
public final class LogRedactionUtil {

	private static final Pattern BEARER = Pattern.compile("(?i)bearer\\s+[^\\s]+");

	private static final Pattern PRIVATE_TOKEN_HDR = Pattern.compile("(?i)private-token:\\s*[^\\s]+");

	private static final Pattern TOKEN_QUERY = Pattern.compile("([?&])(token|access_token|key|secret|password)=([^&\\s]+)");

	private static final Pattern GITHUB_CLASSIC = Pattern.compile("gh[pousr]_[A-Za-z0-9_]+");

	private LogRedactionUtil() {
	}

	public static String safeForLog(String raw) {
		if (raw == null || raw.isEmpty()) {
			return raw;
		}
		String s = raw;
		s = BEARER.matcher(s).replaceAll("Bearer [REDACTED]");
		s = PRIVATE_TOKEN_HDR.matcher(s).replaceAll("Private-Token: [REDACTED]");
		s = TOKEN_QUERY.matcher(s).replaceAll("$1$2=[REDACTED]");
		s = GITHUB_CLASSIC.matcher(s).replaceAll("[REDACTED]");
		// Strip query strings on URLs (may embed tokens)
		s = s.replaceAll("https?://[^\\s]+\\?[^\\s]+", "[URL_WITH_QUERY_REDACTED]");
		return s;
	}
}
