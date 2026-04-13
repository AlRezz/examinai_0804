package com.examinai.app.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Intern UI flags. The assistive AI panel on the intern feedback page was removed in Epic 10; the property
 * remains for configuration compatibility.
 */
@Validated
@ConfigurationProperties(prefix = "examinai.intern")
public class InternUiProperties {

	/** Unused for intern feedback UI after Epic 10 (no AI draft panel). */
	private boolean showAiDraftToIntern = true;

	public boolean isShowAiDraftToIntern() {
		return showAiDraftToIntern;
	}

	public void setShowAiDraftToIntern(boolean showAiDraftToIntern) {
		this.showAiDraftToIntern = showAiDraftToIntern;
	}
}
