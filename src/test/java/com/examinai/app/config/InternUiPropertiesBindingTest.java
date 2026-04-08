package com.examinai.app.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class InternUiPropertiesBindingTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner().withUserConfiguration(PropsConfig.class);

	@Test
	void bindsShowAiDraftFlag() {
		runner.withPropertyValues("examinai.intern.show-ai-draft-to-intern=false")
			.run(ctx -> {
				assertThat(ctx).hasSingleBean(InternUiProperties.class);
				InternUiProperties p = ctx.getBean(InternUiProperties.class);
				assertThat(p.isShowAiDraftToIntern()).isFalse();
			});
	}

	@Configuration
	@EnableConfigurationProperties(InternUiProperties.class)
	static class PropsConfig {
	}
}
