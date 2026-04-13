package com.examinai.app.web.intern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.examinai.app.domain.review.PublishedReview;

class OfficialFeedbackCardSupportTest {

	@Test
	void roseWhenAverageBelowTwoPointFive() {
		assertThat(OfficialFeedbackCardSupport.cssClass(review(1, 2, 3))).contains("rose");
	}

	@Test
	void yellowWhenAverageBetweenTwoPointFiveAndFourInclusive() {
		assertThat(OfficialFeedbackCardSupport.cssClass(review(3, 3, 3))).contains("yellow");
		assertThat(OfficialFeedbackCardSupport.cssClass(review(5, 5, 2))).contains("yellow");
	}

	@Test
	void greenWhenAverageAboveFour() {
		assertThat(OfficialFeedbackCardSupport.cssClass(review(5, 5, 5))).contains("green");
		assertThat(OfficialFeedbackCardSupport.cssClass(review(5, 4, 5))).contains("green");
	}

	private static PublishedReview review(int q, int r, int c) {
		PublishedReview pr = mock(PublishedReview.class);
		when(pr.getQualityScore()).thenReturn(q);
		when(pr.getReadabilityScore()).thenReturn(r);
		when(pr.getCorrectnessScore()).thenReturn(c);
		return pr;
	}
}
