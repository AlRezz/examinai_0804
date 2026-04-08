package com.examinai.app.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class GitProviderPropertiesBindingTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner().withUserConfiguration(PropsConfig.class);

	@Test
	void bindsExaminaiGitPrefix() {
		runner.withPropertyValues("examinai.git.base-url=https://api.github.com", "examinai.git.token=tok-test",
				"examinai.git.read-timeout-seconds=30", "examinai.git.max-retries=3", "examinai.git.retry-backoff-ms=100")
			.run(ctx -> {
				assertThat(ctx).hasSingleBean(GitProviderProperties.class);
				GitProviderProperties p = ctx.getBean(GitProviderProperties.class);
				assertThat(p.getBaseUrl()).isEqualTo("https://api.github.com");
				assertThat(p.getToken()).isEqualTo("tok-test");
				assertThat(p.getReadTimeoutSeconds()).isEqualTo(30);
				assertThat(p.getMaxRetries()).isEqualTo(3);
				assertThat(p.getRetryBackoffMs()).isEqualTo(100);
			});
	}

	@Configuration
	@EnableConfigurationProperties(GitProviderProperties.class)
	static class PropsConfig {
	}
}
