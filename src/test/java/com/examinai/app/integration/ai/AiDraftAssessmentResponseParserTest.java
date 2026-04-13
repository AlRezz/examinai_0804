package com.examinai.app.integration.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AiDraftAssessmentResponseParserTest {

	@Test
	void parsesScoresAndNarrativeBlocks() {
		String raw = """
				Quality: 3
				Readability: 4
				Correctness: 5

				## Feedback on the code
				Line A.

				## Suggestions to improve
				Line B.
				""";
		AiDraftAssessmentResult r = AiDraftAssessmentResponseParser.parse(raw);
		assertThat(r.fullAssessmentText()).contains("Quality: 3");
		assertThat(r.qualityScore()).isEqualTo(3);
		assertThat(r.readabilityScore()).isEqualTo(4);
		assertThat(r.correctnessScore()).isEqualTo(5);
		assertThat(r.narrativeFeedback()).contains("Line A.").contains("Line B.");
	}

	@Test
	void scoresMayAppearInAnyOrderAmongFirstLines() {
		String raw = """
				Correctness: 2
				Quality: 5
				Readability: 1

				## Feedback on the code
				X
				## Suggestions to improve
				Y
				""";
		AiDraftAssessmentResult r = AiDraftAssessmentResponseParser.parse(raw);
		assertThat(r.qualityScore()).isEqualTo(5);
		assertThat(r.readabilityScore()).isEqualTo(1);
		assertThat(r.correctnessScore()).isEqualTo(2);
	}

	@Test
	void narrativeFallsBackToRemainderWhenNoHeadings() {
		String raw = """
				Quality: 3
				Readability: 3
				Correctness: 3

				Free text only.
				""";
		AiDraftAssessmentResult r = AiDraftAssessmentResponseParser.parse(raw);
		assertThat(r.narrativeFeedback()).isEqualTo("Free text only.");
	}
}
