package com.examinai.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Product policy for what interns see (Epic 6.2). When {@code showAiDraft} is {@code false}, AI assistive
 * text is omitted entirely from intern-facing pages regardless of persistence.
 */
@Validated
@ConfigurationProperties(prefix = "examinai.intern")
public class InternUiProperties {

	/**
	 * When true (MVP default), interns may see the latest persisted AI draft as a clearly labeled non-official panel.
	 */
	private boolean showAiDraftToIntern = true;

	public boolean isShowAiDraftToIntern() {
		return showAiDraftToIntern;
	}

	public void setShowAiDraftToIntern(boolean showAiDraftToIntern) {
		this.showAiDraftToIntern = showAiDraftToIntern;
	}
}
