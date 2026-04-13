package com.examinai.app.web.intern;

import com.examinai.app.domain.review.PublishedReview;

/**
 * Maps official rubric scores to intern feedback card surface colors (Epic 10).
 */
public final class OfficialFeedbackCardSupport {

	private OfficialFeedbackCardSupport() {
	}

	/**
	 * Average of quality, readability, correctness: &lt; 2.5 rose, 2.5–4 yellow, &gt; 4 green.
	 */
	public static String cssClass(PublishedReview official) {
		double avg = (official.getQualityScore() + official.getReadabilityScore() + official.getCorrectnessScore()) / 3.0;
		if (avg < 2.5) {
			return "intern-official-panel intern-official-panel--rose";
		}
		if (avg <= 4.0) {
			return "intern-official-panel intern-official-panel--yellow";
		}
		return "intern-official-panel intern-official-panel--green";
	}
}
