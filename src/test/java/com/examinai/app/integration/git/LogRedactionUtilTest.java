package com.examinai.app.integration.git;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LogRedactionUtilTest {

	@Test
	void redactsBearerTokenSubstring() {
		String in = "authorization failed: Bearer ghp_supersecret and tail";
		String out = LogRedactionUtil.safeForLog(in);
		assertThat(out).doesNotContain("ghp_supersecret").contains("[REDACTED]");
	}

	@Test
	void redactsStandaloneGithubPatPattern() {
		String in = "error echo ghp_abc123xyz trailing";
		assertThat(LogRedactionUtil.safeForLog(in)).doesNotContain("ghp_abc123xyz");
	}
}
