package com.examinai.app.integration.ai;

import java.util.Optional;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Values stored on {@link com.examinai.app.domain.ai.ModelInvocation} for audits (FR20); avoids coupling persistence to ChatModel internals.
 */
@Component
public class AiModelAuditDescriptor {

	private final String modelName;

	private final Optional<String> modelVersion;

	public AiModelAuditDescriptor(Environment environment, AiDraftAssessmentProperties properties) {
		this.modelName = environment.getProperty("spring.ai.ollama.chat.options.model", "unknown");
		String v = properties.getAuditModelVersion();
		this.modelVersion = StringUtils.hasText(v) ? Optional.of(v.trim()) : Optional.empty();
	}

	public String modelName() {
		return modelName;
	}

	public Optional<String> modelVersion() {
		return modelVersion;
	}
}
