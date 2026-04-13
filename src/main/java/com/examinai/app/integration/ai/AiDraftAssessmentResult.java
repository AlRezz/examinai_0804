package com.examinai.app.integration.ai;

/**
 * Output of {@link AiDraftAssessmentService#generateDraft}: raw model text for audit persistence plus
 * parsed rubric and narrative for the mentor review form (Story 9.6).
 */
public record AiDraftAssessmentResult(
		String fullAssessmentText,
		Integer qualityScore,
		Integer readabilityScore,
		Integer correctnessScore,
		String narrativeFeedback) {
}
