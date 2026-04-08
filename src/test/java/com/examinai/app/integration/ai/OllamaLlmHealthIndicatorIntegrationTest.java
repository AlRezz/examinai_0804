package com.examinai.app.integration.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Opt-in LLM probe is registered when enabled; default profile leaves it off ({@link com.examinai.app.ExaminaiApplicationTests}).
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = { "examinai.ai.llm-health-probe.enabled=true", "spring.ai.ollama.base-url=http://127.0.0.1:9" })
class OllamaLlmHealthIndicatorIntegrationTest {

	@Autowired
	private OllamaLlmHealthIndicator ollamaLlmHealthIndicator;

	@Test
	void probeIsDownWhenOllamaUnreachable() {
		assertThat(ollamaLlmHealthIndicator.health().getStatus()).isEqualTo(Status.DOWN);
	}
}
