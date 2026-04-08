package com.examinai.app.domain.task;

public enum SubmissionStatus {

	/** Intern work not yet submitted for mentor review */
	DRAFT,
	/** In mentor queue: awaiting review */
	SUBMITTED,
	/** Mentor saved rubric / narrative draft */
	UNDER_REVIEW,
	/** Official outcome published (Story 4.4); intern resubmit returns to SUBMITTED */
	OUTCOME_PUBLISHED
}
