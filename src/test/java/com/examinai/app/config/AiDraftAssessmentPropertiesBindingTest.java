package com.examinai.app.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import com.examinai.app.integration.ai.AiDraftAssessmentProperties;

class AiDraftAssessmentPropertiesBindingTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner().withUserConfiguration(PropsConfig.class);

	@Test
	void bindsExaminaiAiDraftPrefix() {
		runner.withPropertyValues("examinai.ai.draft-assessment.max-source-chars=5000",
				"examinai.ai.draft-assessment.request-timeout-seconds=60", "examinai.ai.draft-assessment.max-retries=1",
				"examinai.ai.draft-assessment.retry-backoff-ms=200", "examinai.ai.draft-assessment.max-flash-chars=8192",
				"examinai.ai.draft-assessment.max-inference-wall-seconds=240")
			.run(ctx -> {
				assertThat(ctx).hasSingleBean(AiDraftAssessmentProperties.class);
				AiDraftAssessmentProperties p = ctx.getBean(AiDraftAssessmentProperties.class);
				assertThat(p.getMaxSourceChars()).isEqualTo(5000);
				assertThat(p.getRequestTimeoutSeconds()).isEqualTo(60);
				assertThat(p.getMaxRetries()).isEqualTo(1);
				assertThat(p.getRetryBackoffMs()).isEqualTo(200);
				assertThat(p.getMaxFlashChars()).isEqualTo(8192);
				assertThat(p.getMaxInferenceWallSeconds()).isEqualTo(240);
			});
	}

	@Configuration
	@EnableConfigurationProperties(AiDraftAssessmentProperties.class)
	static class PropsConfig {
	}
}
